package com.korconstructions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korconstructions.model.ReceiptOcrData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.math.BigDecimal;

@Service
public class ReceiptAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptAnalyzerService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ReceiptAnalyzerService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For Java 8 date/time support
    }

    public BigDecimal extractTotalAmount(File receiptImage) {
        try {
            logger.info("Analyzing receipt image: {}", receiptImage.getName());

            String prompt = """
                Analyze this receipt image and extract ONLY the final total amount.
                Return just the number without currency symbols or text.
                For example, if the total is €45.50, return: 45.50
                If you cannot find a total amount, return: 0
                """;

            String response = chatClient.prompt()
                .user(userSpec -> userSpec
                    .text(prompt)
                    .media(new Media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(receiptImage)))
                )
                .call()
                .content();

            logger.info("Gemini response: {}", response);

            // Clean the response and parse to BigDecimal
            String cleanedResponse = response.trim().replaceAll("[^0-9.]", "");
            BigDecimal amount = new BigDecimal(cleanedResponse);

            logger.info("Extracted amount: €{}", amount);
            return amount;

        } catch (Exception e) {
            logger.error("Error analyzing receipt", e);
            return BigDecimal.ZERO;
        }
    }

    public ReceiptOcrData extractAllReceiptData(File receiptImage) {
        try {
            logger.info("Extracting all data from receipt: {}", receiptImage.getName());

            String prompt = """
                Analyze this receipt image and extract the following information in JSON format:
                {
                  "vendor": "vendor/company name",
                  "date": "date in YYYY-MM-DD format",
                  "totalAmount": "total amount as number (without currency symbols)",
                  "items": ["list of items purchased"],
                  "tax": "tax amount if visible as number (without currency symbols)"
                }
                Return ONLY the JSON, nothing else. If a field is not found, use null for strings/objects or 0 for numbers.
                """;

            String response = chatClient.prompt()
                .user(userSpec -> userSpec
                    .text(prompt)
                    .media(new Media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(receiptImage)))
                )
                .call()
                .content();

            logger.info("Extracted receipt data: {}", response);

            // Clean response - remove markdown code blocks if present
            String cleanedResponse = response.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            // Parse JSON to ReceiptOcrData
            ReceiptOcrData ocrData = objectMapper.readValue(cleanedResponse, ReceiptOcrData.class);
            logger.info("Parsed OCR data - Vendor: {}, Date: {}, Amount: €{}",
                ocrData.getVendor(), ocrData.getDate(), ocrData.getTotalAmount());

            return ocrData;

        } catch (Exception e) {
            logger.error("Error extracting receipt data", e);
            // Return empty data object on error
            return new ReceiptOcrData(null, null, BigDecimal.ZERO, new String[0], BigDecimal.ZERO);
        }
    }
}
