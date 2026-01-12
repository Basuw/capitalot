package com.capitalot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDto {
    private Long id;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private LocalDateTime purchaseDate;
    private String notes;
    private Double currentPrice;
    private Double currentValue;
    private Double gainLoss;
    private Double gainLossPercentage;
}
