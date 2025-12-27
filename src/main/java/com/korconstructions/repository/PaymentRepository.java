package com.korconstructions.repository;

import com.korconstructions.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // JpaRepository provides: findAll(), findById(), save(), deleteById(), existsById()
    // No additional methods needed
}
