package com.capitalot.service;

import com.capitalot.dto.PricePointDto;
import com.capitalot.dto.StockPerformanceMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockMetricsService {
    
    private final StockPriceService stockPriceService;
    
    public StockPerformanceMetrics calculateMetrics(String symbol, String period, String benchmarkSymbol) {
        // Get stock price history
        List<PricePointDto> priceHistory = stockPriceService.getStockHistory(symbol, period);
        
        if (priceHistory.isEmpty() || priceHistory.size() < 2) {
            return null;
        }
        
        // Basic calculations
        double firstPrice = priceHistory.get(0).getPrice();
        double lastPrice = priceHistory.get(priceHistory.size() - 1).getPrice();
        double priceChange = lastPrice - firstPrice;
        double percentChange = (priceChange / firstPrice) * 100;
        
        // Calculate days in period
        LocalDateTime startDate = priceHistory.get(0).getTimestamp();
        LocalDateTime endDate = priceHistory.get(priceHistory.size() - 1).getTimestamp();
        long daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysInPeriod == 0) daysInPeriod = 1;
        
        // Calculate annualized return
        double annualizedReturn = calculateAnnualizedReturn(percentChange, daysInPeriod);
        
        // Calculate benchmark comparison
        Double benchmarkPercentChange = null;
        Double relativePerformance = null;
        Boolean outperformingBenchmark = null;
        
        if (benchmarkSymbol != null && !benchmarkSymbol.isEmpty()) {
            try {
                List<PricePointDto> benchmarkHistory = stockPriceService.getStockHistory(benchmarkSymbol, period);
                if (!benchmarkHistory.isEmpty() && benchmarkHistory.size() >= 2) {
                    double benchmarkFirst = benchmarkHistory.get(0).getPrice();
                    double benchmarkLast = benchmarkHistory.get(benchmarkHistory.size() - 1).getPrice();
                    benchmarkPercentChange = ((benchmarkLast - benchmarkFirst) / benchmarkFirst) * 100;
                    relativePerformance = percentChange - benchmarkPercentChange;
                    outperformingBenchmark = relativePerformance > 0;
                }
            } catch (Exception e) {
                log.warn("Failed to fetch benchmark data for {}", benchmarkSymbol, e);
            }
        }
        
        // Find best and worst days
        StockPerformanceMetrics.DayPerformance bestDay = null;
        StockPerformanceMetrics.DayPerformance worstDay = null;
        
        if (priceHistory.size() > 1) {
            double maxDailyChange = Double.NEGATIVE_INFINITY;
            double minDailyChange = Double.POSITIVE_INFINITY;
            int bestDayIndex = 0;
            int worstDayIndex = 0;
            
            for (int i = 1; i < priceHistory.size(); i++) {
                double prevPrice = priceHistory.get(i - 1).getPrice();
                double currPrice = priceHistory.get(i).getPrice();
                double dailyChange = currPrice - prevPrice;
                double dailyPercentChange = (dailyChange / prevPrice) * 100;
                
                if (dailyPercentChange > maxDailyChange) {
                    maxDailyChange = dailyPercentChange;
                    bestDayIndex = i;
                }
                
                if (dailyPercentChange < minDailyChange) {
                    minDailyChange = dailyPercentChange;
                    worstDayIndex = i;
                }
            }
            
            if (bestDayIndex > 0) {
                PricePointDto bestDayData = priceHistory.get(bestDayIndex);
                double prevPrice = priceHistory.get(bestDayIndex - 1).getPrice();
                double currPrice = bestDayData.getPrice();
                bestDay = StockPerformanceMetrics.DayPerformance.builder()
                        .date(bestDayData.getTimestamp().toString())
                        .priceChange(currPrice - prevPrice)
                        .percentChange(maxDailyChange)
                        .build();
            }
            
            if (worstDayIndex > 0) {
                PricePointDto worstDayData = priceHistory.get(worstDayIndex);
                double prevPrice = priceHistory.get(worstDayIndex - 1).getPrice();
                double currPrice = worstDayData.getPrice();
                worstDay = StockPerformanceMetrics.DayPerformance.builder()
                        .date(worstDayData.getTimestamp().toString())
                        .priceChange(currPrice - prevPrice)
                        .percentChange(minDailyChange)
                        .build();
            }
        }
        
        return StockPerformanceMetrics.builder()
                .firstPrice(firstPrice)
                .lastPrice(lastPrice)
                .priceChange(priceChange)
                .percentChange(percentChange)
                .isPositive(priceChange >= 0)
                .annualizedReturn(annualizedReturn)
                .daysInPeriod((int) daysInPeriod)
                .benchmarkPercentChange(benchmarkPercentChange)
                .relativePerformance(relativePerformance)
                .outperformingBenchmark(outperformingBenchmark)
                .bestDay(bestDay)
                .worstDay(worstDay)
                .build();
    }
    
    private double calculateAnnualizedReturn(double percentChange, long days) {
        if (days <= 0) return 0.0;
        
        // Annualized return formula: ((1 + return)^(365/days)) - 1
        double totalReturn = 1 + (percentChange / 100);
        double yearsEquivalent = 365.0 / days;
        double annualizedReturn = (Math.pow(totalReturn, yearsEquivalent) - 1) * 100;
        
        return annualizedReturn;
    }
}
