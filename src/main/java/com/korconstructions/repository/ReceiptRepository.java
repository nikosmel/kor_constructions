package com.korconstructions.repository;

import com.korconstructions.model.Receipt;

import java.util.List;
import java.util.Optional;

public interface ReceiptRepository {

    List<Receipt> findAll();

    Optional<Receipt> findById(Long id);

    List<Receipt> findByCustomerId(Long customerId);

    Receipt save(Receipt receipt);

    void deleteById(Long id);

    boolean existsById(Long id);
}
