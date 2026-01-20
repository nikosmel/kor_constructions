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

        // Auto-generate receipt number if not provided
        if (receipt.getReceiptNumber() == null || receipt.getReceiptNumber().trim().isEmpty()) {
            receipt.setReceiptNumber(generateNextReceiptNumber());
        }

        return receiptRepository.save(receipt);
    }

    private String generateNextReceiptNumber() {
        Optional<Receipt> lastReceipt = receiptRepository.findTopByOrderByIdDesc();

        if (lastReceipt.isPresent() && lastReceipt.get().getReceiptNumber() != null) {
            String lastNumber = lastReceipt.get().getReceiptNumber();
            try {
                // Try to parse as integer and increment
                int number = Integer.parseInt(lastNumber);
                return String.valueOf(number + 1);
            } catch (NumberFormatException e) {
                // If not a number, try to extract number from end
                String digits = lastNumber.replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    int number = Integer.parseInt(digits);
                    return String.valueOf(number + 1);
                }
            }
        }

        // Default starting number
        return "1";
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

    public String getNextReceiptNumber() {
        return generateNextReceiptNumber();
    }
}
