package com.korconstructions.service;

import com.korconstructions.model.Receipt;
import com.korconstructions.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    public Optional<Receipt> getReceiptById(Long id) {
        return receiptRepository.findById(id);
    }

    public List<Receipt> getReceiptsByCustomerId(Long customerId) {
        return receiptRepository.findByCustomerId(customerId);
    }

    public Receipt createReceipt(Receipt receipt) {
        receipt.setId(null);
        return receiptRepository.save(receipt);
    }

    public Receipt updateReceipt(Long id, Receipt receipt) {
        if (!receiptRepository.existsById(id)) {
            throw new RuntimeException("Receipt not found with id: " + id);
        }
        receipt.setId(id);
        return receiptRepository.save(receipt);
    }

    public void deleteReceipt(Long id) {
        if (!receiptRepository.existsById(id)) {
            throw new RuntimeException("Receipt not found with id: " + id);
        }
        receiptRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return receiptRepository.existsById(id);
    }
}
