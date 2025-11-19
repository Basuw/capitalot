package com.capitalot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioPerformanceDto {
    private LocalDateTime timestamp;
    private Double totalValue;
    private Double gainLoss;
}
