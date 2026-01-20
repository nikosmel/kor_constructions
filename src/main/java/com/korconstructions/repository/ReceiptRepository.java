package com.korconstructions.repository;

import com.korconstructions.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    // JpaRepository provides: findAll(), findById(), save(), deleteById(), existsById()

    // Custom query method - Spring Data JPA will automatically implement this
    List<Receipt> findByCustomerId(Long customerId);

    // Find the last receipt ordered by ID to get the latest receipt number
    Optional<Receipt> findTopByOrderByIdDesc();
}
