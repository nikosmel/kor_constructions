package com.korconstructions.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String customerName; // Denormalized for display
    private String payeeName; // Person/Company we're paying to (kept for backward compatibility)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String paymentNumber;
    private BigDecimal amount;
    private String reason;
    private String signature1; // Name of first signatory
    private String signature2; // Name of second signatory

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public Payment(String payeeName, LocalDate date, String paymentNumber,
                   BigDecimal amount, String reason, String signature1, String signature2) {
        this.payeeName = payeeName;
        this.date = date;
        this.paymentNumber = paymentNumber;
        this.amount = amount;
        this.reason = reason;
        this.signature1 = signature1;
        this.signature2 = signature2;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
