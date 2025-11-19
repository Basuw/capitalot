package com.capitalot.service;

import com.capitalot.dto.PerformanceStats;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.PortfolioStock;
import com.capitalot.model.User;
import com.capitalot.repository.PortfolioStockRepository;
import com.capitalot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    
    private final PortfolioStockRepository portfolioStockRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;
    
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
        return calculateStats(email, LocalDateTime.MIN, LocalDateTime.MAX);
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
}
