package com.korconstructions.controller;

import com.korconstructions.service.DropboxBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    @Autowired
    private DropboxBackupService dropboxBackupService;

    @Value("${app.data.directory}")
    private String dataDirectory;

    @Value("${app.data.file}")
    private String itemsFile;

    @Value("${app.data.customers-file}")
    private String customersFile;

    @Value("${app.data.receipts-file}")
    private String receiptsFile;

    @Value("${app.data.payments-file}")
    private String paymentsFile;

    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreFromDropbox() {
        Map<String, Object> response = new HashMap<>();

        if (!dropboxBackupService.isEnabled()) {
            response.put("success", false);
            response.put("message", "Dropbox backup is not enabled");
            return ResponseEntity.ok(response);
        }

        try {
            int restoredCount = 0;
            StringBuilder details = new StringBuilder();

            // Restore all files
            String[] files = {itemsFile, customersFile, receiptsFile, paymentsFile};
            for (String fileName : files) {
                File targetFile = new File(dataDirectory + File.separator + fileName);
                boolean restored = dropboxBackupService.restoreFile(fileName, targetFile);
                if (restored) {
                    restoredCount++;
                    details.append(fileName).append(" ✓ ");
                    logger.info("Manually restored {} from Dropbox", fileName);
                } else {
                    details.append(fileName).append(" ✗ ");
                }
            }

            if (restoredCount > 0) {
                response.put("success", true);
                response.put("message", "Restored " + restoredCount + " file(s) from Dropbox");
                response.put("details", details.toString());
                logger.info("Manual restore completed: {} files restored", restoredCount);
            } else {
                response.put("success", false);
                response.put("message", "No files were restored from Dropbox");
                response.put("details", details.toString());
            }

        } catch (Exception e) {
            logger.error("Error during manual restore from Dropbox", e);
            response.put("success", false);
            response.put("message", "Error restoring from Dropbox: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getBackupStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", dropboxBackupService.isEnabled());
        return ResponseEntity.ok(response);
    }
}
