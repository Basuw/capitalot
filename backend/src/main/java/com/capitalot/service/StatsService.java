package com.capitalot.service;

import com.capitalot.dto.PerformanceStats;
import com.capitalot.dto.PortfolioPerformanceDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.PortfolioHistory;
import com.capitalot.model.PortfolioStock;
import com.capitalot.model.User;
import com.capitalot.repository.PortfolioHistoryRepository;
import com.capitalot.repository.PortfolioStockRepository;
import com.capitalot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatsService {
    
    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioHistoryRepository portfolioHistoryRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;
    private final Random random = new Random();
    
    public PerformanceStats getDailyStats(String email) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return calculateStats(email, startOfDay, endOfDay);
    }
    
    public PerformanceStats getMonthlyStats(String email) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().atTime(LocalTime.MAX);
        return calculateStats(email, startOfMonth, endOfMonth);
    }
    
    public PerformanceStats getYearlyStats(String email) {
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.now().atTime(LocalTime.MAX);
        return calculateStats(email, startOfYear, endOfYear);
    }
    
    public PerformanceStats getAllTimeStats(String email) {
        LocalDateTime minDate = LocalDateTime.of(1900, 1, 1, 0, 0);
        LocalDateTime maxDate = LocalDateTime.of(2100, 12, 31, 23, 59);
        return calculateStats(email, minDate, maxDate);
    }
    
    public List<PortfolioPerformanceDto> getPerformanceHistory(String email, String period) {
        LocalDateTime startDate = getStartDateFromPeriod(period);
        List<PortfolioHistory> history = portfolioHistoryRepository
            .findByUserEmailAndTimestampAfter(email, startDate);
        
        if (history.isEmpty()) {
            return generateMockPerformanceHistory(email, period);
        }
        
        return history.stream()
            .map(h -> PortfolioPerformanceDto.builder()
                .timestamp(h.getTimestamp())
                .totalValue(h.getTotalValue())
                .gainLoss(h.getGainLoss())
                .build())
            .toList();
    }
    
    private PerformanceStats calculateStats(String email, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<PortfolioStock> stocks = portfolioStockRepository
            .findByUserIdAndDateRange(user.getId(), startDate, endDate);
        
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        
        for (PortfolioStock ps : stocks) {
            BigDecimal invested = ps.getPurchasePrice().multiply(ps.getQuantity());
            totalInvested = totalInvested.add(invested);
            
            StockPriceResponse priceResponse = stockPriceService.getStockPrice(ps.getStock().getSymbol());
            BigDecimal current = priceResponse.getCurrentPrice().multiply(ps.getQuantity());
            currentValue = currentValue.add(current);
        }
        
        BigDecimal gainLoss = currentValue.subtract(totalInvested);
        BigDecimal gainLossPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
            ? gainLoss.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        return PerformanceStats.builder()
            .totalInvested(totalInvested)
            .currentValue(currentValue)
            .totalGainLoss(gainLoss)
            .totalGainLossPercent(gainLossPercent)
            .numberOfStocks(stocks.size())
            .build();
    }
    
    private LocalDateTime getStartDateFromPeriod(String period) {
        return switch (period) {
            case "1D" -> LocalDateTime.now().minusDays(1);
            case "1W" -> LocalDateTime.now().minusWeeks(1);
            case "1M" -> LocalDateTime.now().minusMonths(1);
            case "3M" -> LocalDateTime.now().minusMonths(3);
            case "6M" -> LocalDateTime.now().minusMonths(6);
            case "1Y" -> LocalDateTime.now().minusYears(1);
            case "5Y" -> LocalDateTime.now().minusYears(5);
            default -> LocalDateTime.now().minusMonths(1);
        };
    }
    
    private List<PortfolioPerformanceDto> generateMockPerformanceHistory(String email, String period) {
        List<PortfolioPerformanceDto> points = new ArrayList<>();
        LocalDateTime startDate = getStartDateFromPeriod(period);
        LocalDateTime now = LocalDateTime.now();
        
        double baseValue = 10000 + random.nextDouble() * 40000;
        int dataPoints = getDataPointsForPeriod(period);
        long totalMinutes = java.time.Duration.between(startDate, now).toMinutes();
        long intervalMinutes = totalMinutes / dataPoints;
        
        for (int i = 0; i < dataPoints; i++) {
            LocalDateTime timestamp = startDate.plusMinutes(intervalMinutes * i);
            double variation = (random.nextDouble() - 0.4) * 1000;
            double value = baseValue + variation;
            double gainLoss = value - 10000;
            
            points.add(PortfolioPerformanceDto.builder()
                .timestamp(timestamp)
                .totalValue(value)
                .gainLoss(gainLoss)
                .build());
            
            baseValue = value;
        }
        
        return points;
    }
    
    private int getDataPointsForPeriod(String period) {
        return switch (period) {
            case "1D" -> 24;
            case "1W" -> 7 * 4;
            case "1M" -> 30;
            case "3M" -> 90;
            case "6M" -> 180;
            case "1Y" -> 365;
            case "5Y" -> 365 * 5 / 7;
            default -> 30;
        };
    }
}
