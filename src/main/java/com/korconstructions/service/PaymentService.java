package com.korconstructions.service;

import com.korconstructions.model.Payment;
import com.korconstructions.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment createPayment(Payment payment) {
        payment.setId(null);

        // Auto-generate payment number if not provided
        if (payment.getPaymentNumber() == null || payment.getPaymentNumber().trim().isEmpty()) {
            payment.setPaymentNumber(generateNextPaymentNumber());
        }

        return paymentRepository.save(payment);
    }

    private String generateNextPaymentNumber() {
        Optional<Payment> lastPayment = paymentRepository.findTopByOrderByIdDesc();

        if (lastPayment.isPresent() && lastPayment.get().getPaymentNumber() != null) {
            String lastNumber = lastPayment.get().getPaymentNumber();
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

    public Payment updatePayment(Long id, Payment payment) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        payment.setId(id);
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return paymentRepository.existsById(id);
    }

    public String getNextPaymentNumber() {
        return generateNextPaymentNumber();
    }
}
