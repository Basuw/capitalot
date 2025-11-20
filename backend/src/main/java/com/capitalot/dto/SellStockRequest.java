package com.capitalot.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SellStockRequest {
    private BigDecimal salePrice;
    private LocalDateTime saleDate;
}
