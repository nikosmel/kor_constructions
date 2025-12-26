package com.korconstructions.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.korconstructions.model.Item;
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
public class FileItemRepository implements ItemRepository {

    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final DropboxBackupService dropboxBackupService;

    @Value("${app.data.directory}")
    private String dataDirectory;

    @Value("${app.data.file}")
    private String dataFile;

    private File file;

    @Autowired
    public FileItemRepository(DropboxBackupService dropboxBackupService) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.dropboxBackupService = dropboxBackupService;
    }

    @PostConstruct
    public void init() throws IOException {
        Path dirPath = Paths.get(dataDirectory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        file = new File(dataDirectory + File.separator + dataFile);

        // Create empty file if it doesn't exist (use manual sync button to restore from Dropbox)
        if (!file.exists()) {
            file.createNewFile();
            writeToFile(new ArrayList<>());
        }

        List<Item> items = readFromFile();
        if (!items.isEmpty()) {
            Long maxId = items.stream()
                    .map(Item::getId)
                    .max(Long::compareTo)
                    .orElse(0L);
            idGenerator.set(maxId + 1);
        }
    }

    @Override
    public List<Item> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Item> findById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Item save(Item item) {
        lock.writeLock().lock();
        try {
            List<Item> items = readFromFile();

            if (item.getId() == null) {
                item.setId(idGenerator.getAndIncrement());
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                items.add(item);
            } else {
                item.setUpdatedAt(LocalDateTime.now());
                items.removeIf(i -> i.getId().equals(item.getId()));
                items.add(item);
            }

            writeToFile(items);
            return item;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(Long id) {
        lock.writeLock().lock();
        try {
            List<Item> items = readFromFile();
            items.removeIf(item -> item.getId().equals(id));
            writeToFile(items);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .anyMatch(item -> item.getId().equals(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Item> readFromFile() {
        try {
            if (file.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<Item>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + file.getAbsolutePath(), e);
        }
    }

    private void writeToFile(List<Item> items) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, items);
            // Backup to Dropbox after successful write
            dropboxBackupService.backupFile(file, dataFile);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getAbsolutePath(), e);
        }
    }
}
