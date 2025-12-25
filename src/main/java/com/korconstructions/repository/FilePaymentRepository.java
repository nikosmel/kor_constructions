package com.korconstructions.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.korconstructions.model.Payment;
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

@Repository
public class FilePaymentRepository implements PaymentRepository {

    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final DropboxBackupService dropboxBackupService;

    @Value("${app.data.directory}")
    private String dataDirectory;

    @Value("${app.data.payments-file}")
    private String dataFile;

    private File file;

    @Autowired
    public FilePaymentRepository(DropboxBackupService dropboxBackupService) {
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

        List<Payment> payments = readFromFile();
        if (!payments.isEmpty()) {
            Long maxId = payments.stream()
                    .map(Payment::getId)
                    .max(Long::compareTo)
                    .orElse(0L);
            idGenerator.set(maxId + 1);
        }
    }

    @Override
    public List<Payment> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(payment -> payment.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Payment save(Payment payment) {
        lock.writeLock().lock();
        try {
            List<Payment> payments = readFromFile();

            if (payment.getId() == null) {
                payment.setId(idGenerator.getAndIncrement());
                payment.setCreatedAt(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());
                payments.add(payment);
            } else {
                payment.setUpdatedAt(LocalDateTime.now());
                payments.removeIf(p -> p.getId().equals(payment.getId()));
                payments.add(payment);
            }

            writeToFile(payments);
            return payment;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(Long id) {
        lock.writeLock().lock();
        try {
            List<Payment> payments = readFromFile();
            payments.removeIf(payment -> payment.getId().equals(id));
            writeToFile(payments);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .anyMatch(payment -> payment.getId().equals(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Payment> readFromFile() {
        try {
            if (file.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<Payment>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + file.getAbsolutePath(), e);
        }
    }

    private void writeToFile(List<Payment> payments) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, payments);
            // Backup to Dropbox after successful write
            dropboxBackupService.backupFile(file, dataFile);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getAbsolutePath(), e);
        }
    }
}
