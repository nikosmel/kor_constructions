package com.korconstructions.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    private Long id;
    private Long customerId;
    private String customerName; // Denormalized for display

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String receiptNumber;
    private BigDecimal amount;
    private String reason;
    private String signature1; // Name of first signatory
    private String signature2; // Name of second signatory

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public Receipt(Long customerId, String customerName, LocalDate date, String receiptNumber,
                   BigDecimal amount, String reason, String signature1, String signature2) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.date = date;
        this.receiptNumber = receiptNumber;
        this.amount = amount;
        this.reason = reason;
        this.signature1 = signature1;
        this.signature2 = signature2;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
