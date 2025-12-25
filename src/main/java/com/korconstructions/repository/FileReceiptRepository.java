package com.korconstructions.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.korconstructions.model.Receipt;
import com.korconstructions.service.DropboxBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class FileReceiptRepository implements ReceiptRepository {

    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final DropboxBackupService dropboxBackupService;

    @Value("${app.data.directory}")
    private String dataDirectory;

    @Value("${app.data.receipts-file}")
    private String dataFile;

    private File file;

    @Autowired
    public FileReceiptRepository(DropboxBackupService dropboxBackupService) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.dropboxBackupService = dropboxBackupService;
    }

    @PostConstruct
    public void init() throws IOException {
        Path dirPath = Paths.get(dataDirectory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        file = new File(dataDirectory + File.separator + dataFile);

        // Try to restore from Dropbox if local file doesn't exist or is empty
        if (!file.exists() || file.length() == 0) {
            if (dropboxBackupService.isEnabled()) {
                boolean restored = dropboxBackupService.restoreFile(dataFile, file);
                if (!restored && !file.exists()) {
                    file.createNewFile();
                    writeToFile(new ArrayList<>());
                }
            } else if (!file.exists()) {
                file.createNewFile();
                writeToFile(new ArrayList<>());
            }
        }

        List<Receipt> receipts = readFromFile();
        if (!receipts.isEmpty()) {
            Long maxId = receipts.stream()
                    .map(Receipt::getId)
                    .max(Long::compareTo)
                    .orElse(0L);
            idGenerator.set(maxId + 1);
        }
    }

    @Override
    public List<Receipt> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Receipt> findById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(receipt -> receipt.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Receipt> findByCustomerId(Long customerId) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(receipt -> receipt.getCustomerId().equals(customerId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Receipt save(Receipt receipt) {
        lock.writeLock().lock();
        try {
            List<Receipt> receipts = readFromFile();

            if (receipt.getId() == null) {
                receipt.setId(idGenerator.getAndIncrement());
                receipt.setCreatedAt(LocalDateTime.now());
                receipt.setUpdatedAt(LocalDateTime.now());
                receipts.add(receipt);
            } else {
                receipt.setUpdatedAt(LocalDateTime.now());
                receipts.removeIf(r -> r.getId().equals(receipt.getId()));
                receipts.add(receipt);
            }

            writeToFile(receipts);
            return receipt;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(Long id) {
        lock.writeLock().lock();
        try {
            List<Receipt> receipts = readFromFile();
            receipts.removeIf(receipt -> receipt.getId().equals(id));
            writeToFile(receipts);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .anyMatch(receipt -> receipt.getId().equals(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Receipt> readFromFile() {
        try {
            if (file.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<Receipt>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + file.getAbsolutePath(), e);
        }
    }

    private void writeToFile(List<Receipt> receipts) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, receipts);
            // Backup to Dropbox after successful write
            dropboxBackupService.backupFile(file, dataFile);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getAbsolutePath(), e);
        }
    }
}
