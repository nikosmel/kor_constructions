package com.korconstructions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryDTO {
    private BigDecimal startingCapital;
    private BigDecimal squareMeters;
    private BigDecimal totalExpenses;
}
