package com.capitalot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceStats {
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal totalGainLoss;
    private BigDecimal totalGainLossPercent;
    private Integer numberOfStocks;
    private List<PortfolioPerformanceDto> performanceHistory;
}
