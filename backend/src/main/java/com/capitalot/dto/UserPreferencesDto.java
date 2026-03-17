package com.capitalot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDto {
    private Long id;
    private Boolean showAnnualizedReturn;
    private Boolean showBenchmarkComparison;
    private Boolean showBestWorstDay;
    private Boolean showStartPriceLine;
    private String benchmarkSymbol;
    private String currency;
    private Boolean showPerformanceBadge;
    private Boolean showDetailedMetrics;
}
