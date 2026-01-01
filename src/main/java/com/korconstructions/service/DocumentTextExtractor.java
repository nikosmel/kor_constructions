package com.korconstructions.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Service
public class DocumentTextExtractor {

    /**
     * Extracts text from a document file (PDF, DOCX, TXT, etc.)
     */
    public String extractText(File file) {
        log.info("=== Starting text extraction ===");
        log.info("File: {}, Exists: {}, Size: {} bytes", file.getName(), file.exists(), file.length());

        String mimeType = detectMimeType(file);
        log.info("Detected MIME type: {}", mimeType);

        // Try PDF extraction first if it's a PDF
        if ("application/pdf".equals(mimeType)) {
            log.info("Attempting PDF extraction with PDFBox...");
            String pdfText = extractTextFromPDF(file);
            if (pdfText != null && !pdfText.trim().isEmpty()) {
                log.info("PDFBox extraction successful: {} characters", pdfText.length());
                return pdfText;
            }
            log.warn("PDFBox extraction returned empty text, falling back to Tika");
        }

        // Fall back to Apache Tika for other formats
        log.info("Attempting extraction with Apache Tika...");
        String tikaText = extractTextWithTika(file);
        log.info("Tika extraction result: {} characters", tikaText != null ? tikaText.length() : 0);
        return tikaText;
    }

    /**
     * Extract text from PDF using PDFBox
     */
    private String extractTextFromPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            log.info("PDF loaded successfully, pages: {}", document.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDFBox extracted {} characters", text != null ? text.length() : 0);
            return text;
        } catch (Exception e) {
            log.error("PDFBox extraction failed for {}: {} - {}",
                file.getName(), e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Extract text using Apache Tika (supports many formats)
     */
    private String extractTextWithTika(File file) {
        try (InputStream stream = new FileInputStream(file)) {
            BodyContentHandler handler = new BodyContentHandler(-1); // No limit
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();

            log.info("Starting Tika parse...");
            parser.parse(stream, handler, metadata);
            String text = handler.toString();
            log.info("Tika extracted {} characters", text != null ? text.length() : 0);

            // Log some metadata
            log.info("Content-Type from Tika: {}", metadata.get("Content-Type"));

            return text;
        } catch (Exception e) {
            log.error("Tika extraction failed for {}: {} - {}",
                file.getName(), e.getClass().getSimpleName(), e.getMessage(), e);
            return "";
        }
    }

    /**
     * Detect MIME type of file
     */
    private String detectMimeType(File file) {
        try (InputStream stream = new FileInputStream(file)) {
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(stream, new BodyContentHandler(-1), metadata);
            return metadata.get("Content-Type");
        } catch (Exception e) {
            log.error("Failed to detect MIME type for: {}", file.getName(), e);
            return "application/octet-stream";
        }
    }
}
