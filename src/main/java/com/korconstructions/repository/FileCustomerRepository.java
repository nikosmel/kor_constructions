package com.korconstructions.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.korconstructions.model.Customer;
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
public class FileCustomerRepository implements CustomerRepository {

    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final DropboxBackupService dropboxBackupService;

    @Value("${app.data.directory}")
    private String dataDirectory;

    @Value("${app.data.customers-file}")
    private String dataFile;

    private File file;

    @Autowired
    public FileCustomerRepository(DropboxBackupService dropboxBackupService) {
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

        List<Customer> customers = readFromFile();
        if (!customers.isEmpty()) {
            Long maxId = customers.stream()
                    .map(Customer::getId)
                    .max(Long::compareTo)
                    .orElse(0L);
            idGenerator.set(maxId + 1);
        }
    }

    @Override
    public List<Customer> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(customer -> customer.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Customer save(Customer customer) {
        lock.writeLock().lock();
        try {
            List<Customer> customers = readFromFile();

            if (customer.getId() == null) {
                customer.setId(idGenerator.getAndIncrement());
                customer.setCreatedAt(LocalDateTime.now());
                customer.setUpdatedAt(LocalDateTime.now());
                customers.add(customer);
            } else {
                customer.setUpdatedAt(LocalDateTime.now());
                customers.removeIf(c -> c.getId().equals(customer.getId()));
                customers.add(customer);
            }

            writeToFile(customers);
            return customer;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(Long id) {
        lock.writeLock().lock();
        try {
            List<Customer> customers = readFromFile();
            customers.removeIf(customer -> customer.getId().equals(id));
            writeToFile(customers);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsById(Long id) {
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .anyMatch(customer -> customer.getId().equals(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Customer> readFromFile() {
        try {
            if (file.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<Customer>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + file.getAbsolutePath(), e);
        }
    }

    private void writeToFile(List<Customer> customers) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, customers);
            // Backup to Dropbox after successful write
            dropboxBackupService.backupFile(file, dataFile);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getAbsolutePath(), e);
        }
    }
}
