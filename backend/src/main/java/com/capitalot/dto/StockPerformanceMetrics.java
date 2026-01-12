package com.capitalot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPerformanceMetrics {
    // Basic metrics
    private Double firstPrice;
    private Double lastPrice;
    private Double priceChange;
    private Double percentChange;
    private Boolean isPositive;
    
    // Advanced metrics
    private Double annualizedReturn;
    private Integer daysInPeriod;
    
    // Benchmark comparison (vs S&P 500)
    private Double benchmarkPercentChange;
    private Double relativePerformance;
    private Boolean outperformingBenchmark;
    
    // Best/Worst days
    private DayPerformance bestDay;
    private DayPerformance worstDay;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPerformance {
        private String date;
        private Double priceChange;
        private Double percentChange;
    }
}
