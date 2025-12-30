package com.korconstructions.controller;

import com.korconstructions.model.ReceiptOcrData;
import com.korconstructions.service.ReceiptAnalyzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/receipts/ocr")
@CrossOrigin(origins = "*")
public class ReceiptOCRController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptOCRController.class);

    @Autowired
    private ReceiptAnalyzerService receiptAnalyzerService;

    @Value("${app.upload.dir:./uploads/receipts}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAndAnalyze(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Only image files are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            file.transferTo(filePath.toFile());
            logger.info("File saved: {}", filePath);

            // Analyze with AI Vision
            File savedFile = filePath.toFile();
            ReceiptOcrData ocrData = receiptAnalyzerService.extractAllReceiptData(savedFile);

            // Build success response
            response.put("success", true);
            response.put("message", "Receipt analyzed successfully");
            response.put("filename", uniqueFilename);
            response.put("vendor", ocrData.getVendor());
            response.put("date", ocrData.getDate());
            response.put("totalAmount", ocrData.getTotalAmount());
            response.put("items", ocrData.getItems());
            response.put("tax", ocrData.getTax());
            response.put("filePath", filePath.toString());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Error uploading file", e);
            response.put("success", false);
            response.put("message", "Error saving file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            logger.error("Error analyzing receipt", e);
            response.put("success", false);
            response.put("message", "Error analyzing receipt: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/analyze-full")
    public ResponseEntity<Map<String, Object>> analyzeFullReceipt(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Save file temporarily
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFilename = UUID.randomUUID().toString() + ".jpg";
            Path filePath = uploadPath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            // Extract all data
            ReceiptOcrData ocrData = receiptAnalyzerService.extractAllReceiptData(filePath.toFile());

            response.put("success", true);
            response.put("data", ocrData);
            response.put("filename", uniqueFilename);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error analyzing full receipt", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
