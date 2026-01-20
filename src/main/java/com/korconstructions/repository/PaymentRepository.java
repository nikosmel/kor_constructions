package com.korconstructions.repository;

import com.korconstructions.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // JpaRepository provides: findAll(), findById(), save(), deleteById(), existsById()

    // Find the last payment ordered by ID to get the latest payment number
    Optional<Payment> findTopByOrderByIdDesc();
}
