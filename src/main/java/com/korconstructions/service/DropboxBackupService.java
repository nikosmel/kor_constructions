package com.korconstructions.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class DropboxBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DropboxBackupService.class);

    @Value("${app.dropbox.access-token:}")
    private String accessToken;

    @Value("${app.dropbox.enabled:false}")
    private boolean enabled;

    @Value("${app.dropbox.backup-path:/kor-constructions-backup}")
    private String backupPath;

    private DbxClientV2 client;

    @PostConstruct
    public void init() {
        if (!enabled || accessToken == null || accessToken.isEmpty()) {
            logger.warn("Dropbox backup is disabled. Set app.dropbox.enabled=true and app.dropbox.access-token to enable.");
            return;
        }

        try {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("kor-constructions/1.0").build();
            client = new DbxClientV2(config, accessToken);

            // Test connection
            client.users().getCurrentAccount();
            logger.info("Dropbox backup service initialized successfully");
        } catch (DbxException e) {
            logger.error("Failed to initialize Dropbox client. Backup will be disabled.", e);
            enabled = false;
        }
    }

    /**
     * Upload a file to Dropbox
     */
    public void backupFile(File file, String fileName) {
        if (!enabled || client == null) {
            return;
        }

        try (InputStream in = new FileInputStream(file)) {
            String dropboxPath = backupPath + "/" + fileName;

            FileMetadata metadata = client.files()
                .uploadBuilder(dropboxPath)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(in);

            logger.info("Successfully backed up {} to Dropbox: {}", fileName, dropboxPath);
        } catch (DbxException | IOException e) {
            logger.error("Failed to backup {} to Dropbox", fileName, e);
        }
    }

    /**
     * Download a file from Dropbox (for restore)
     */
    public boolean restoreFile(String fileName, File targetFile) {
        if (!enabled || client == null) {
            return false;
        }

        try {
            String dropboxPath = backupPath + "/" + fileName;

            // Create parent directories if they don't exist
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileOutputStream out = new FileOutputStream(targetFile)) {
                client.files().downloadBuilder(dropboxPath).download(out);
                logger.info("Successfully restored {} from Dropbox", fileName);
                return true;
            }
        } catch (DbxException | IOException e) {
            logger.warn("Could not restore {} from Dropbox: {}", fileName, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a file exists in Dropbox
     */
    public boolean fileExistsInDropbox(String fileName) {
        if (!enabled || client == null) {
            return false;
        }

        try {
            String dropboxPath = backupPath + "/" + fileName;
            client.files().getMetadata(dropboxPath);
            return true;
        } catch (DbxException e) {
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
