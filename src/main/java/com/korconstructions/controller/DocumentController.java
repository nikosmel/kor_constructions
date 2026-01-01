package com.korconstructions.controller;

import com.korconstructions.model.Building;
import com.korconstructions.model.Document;
import com.korconstructions.model.DocumentType;
import com.korconstructions.repository.BuildingRepository;
import com.korconstructions.service.DocumentRAGService;
import com.korconstructions.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRAGService ragService;
    private final BuildingRepository buildingRepository;

    @Value("${app.upload.documents-dir:/Users/elenikorovesi/Downloads/korConstructions/uploads/documents}")
    private String documentsUploadDir;

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Document>> getDocumentsByType(@PathVariable DocumentType type) {
        return ResponseEntity.ok(documentService.getDocumentsByType(type));
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<Document>> getDocumentsByBuilding(@PathVariable Long buildingId) {
        return ResponseEntity.ok(documentService.getDocumentsByBuilding(buildingId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String query) {
        return ResponseEntity.ok(documentService.searchDocuments(query));
    }

    @PostMapping
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("type") DocumentType type,
            @RequestParam(value = "buildingId", required = false) Long buildingId) {

        try {
            // Save file to disk
            String filePath = documentService.saveUploadedFile(file, documentsUploadDir);

            // Create document entity
            Document document = new Document();
            document.setTitle(title);
            document.setDescription(description);
            document.setType(type);
            document.setFileName(file.getOriginalFilename());
            document.setFilePath(filePath);
            document.setMimeType(file.getContentType());
            document.setFileSize(file.getSize());

            if (buildingId != null) {
                Building building = buildingRepository.findById(buildingId)
                    .orElseThrow(() -> new RuntimeException("Building not found"));
                document.setBuilding(building);
            }

            // Save document metadata first
            Document savedDocument = documentService.saveDocument(document);

            // Index document for RAG and extract text
            File savedFile = new File(filePath);
            String extractedText = null;
            try {
                extractedText = ragService.indexDocument(savedDocument, savedFile);
                savedDocument.setExtractedText(extractedText);
                log.info("Successfully extracted {} characters from document",
                    extractedText != null ? extractedText.length() : 0);
            } catch (Exception e) {
                log.error("Failed to index document for RAG: {}", savedDocument.getId(), e);
                savedDocument.setExtractedText("Error: " + e.getMessage());
            }

            // Update document with extracted text
            savedDocument = documentService.saveDocument(savedDocument);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);

        } catch (Exception e) {
            log.error("Failed to upload document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @RequestBody Document documentUpdates) {

        Document existingDocument = documentService.getDocumentById(id);

        // Update fields
        if (documentUpdates.getTitle() != null) {
            existingDocument.setTitle(documentUpdates.getTitle());
        }
        if (documentUpdates.getDescription() != null) {
            existingDocument.setDescription(documentUpdates.getDescription());
        }
        if (documentUpdates.getType() != null) {
            existingDocument.setType(documentUpdates.getType());
        }
        if (documentUpdates.getBuilding() != null) {
            existingDocument.setBuilding(documentUpdates.getBuilding());
        }

        Document updated = documentService.saveDocument(existingDocument);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        ragService.deleteDocumentEmbeddings(id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(
            @RequestBody Map<String, Object> request) {

        String question = (String) request.get("question");
        Long buildingId = request.get("buildingId") != null
            ? Long.valueOf(request.get("buildingId").toString())
            : null;

        if (question == null || question.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Question is required");
            return ResponseEntity.badRequest().body(error);
        }

        String answer = ragService.askQuestion(question, buildingId);

        Map<String, String> response = new HashMap<>();
        response.put("question", question);
        response.put("answer", answer);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean inline) {

        try {
            Document document = documentService.getDocumentById(id);
            Path filePath = Paths.get(document.getFilePath());

            if (!Files.exists(filePath)) {
                log.error("File not found on disk: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            // Determine content type
            String contentType = document.getMimeType();
            if (contentType == null) {
                contentType = Files.probeContentType(filePath);
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // For inline viewing (e.g., PDFs in browser) vs. download
            // Use RFC 5987 encoding for UTF-8 filenames (supports Greek characters)
            String encodedFilename = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

            if (inline && contentType.equals("application/pdf")) {
                headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.inline()
                        .filename(document.getFileName(), StandardCharsets.UTF_8)
                        .build()
                );
            } else {
                headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.attachment()
                        .filename(document.getFileName(), StandardCharsets.UTF_8)
                        .build()
                );
            }

            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);

        } catch (Exception e) {
            log.error("Failed to download document: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
