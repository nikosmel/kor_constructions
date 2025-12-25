package com.korconstructions.repository;

import com.korconstructions.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    List<Payment> findAll();

    Optional<Payment> findById(Long id);

    Payment save(Payment payment);

    void deleteById(Long id);

    boolean existsById(Long id);
}
