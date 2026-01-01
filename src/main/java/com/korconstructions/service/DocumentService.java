package com.korconstructions.service;

import com.korconstructions.model.Document;
import com.korconstructions.model.DocumentType;
import com.korconstructions.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByType(DocumentType type) {
        return documentRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByBuilding(Long buildingId) {
        return documentRepository.findByBuildingId(buildingId);
    }

    @Transactional(readOnly = true)
    public List<Document> searchDocuments(String query) {
        return documentRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);

        // Delete physical file if exists
        if (document.getFilePath() != null) {
            try {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.error("Failed to delete file: {}", document.getFilePath(), e);
            }
        }

        documentRepository.deleteById(id);
    }

    public String saveUploadedFile(MultipartFile file, String uploadDir) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }
}
