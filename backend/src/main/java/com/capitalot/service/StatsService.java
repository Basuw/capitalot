package com.capitalot.service;

import com.capitalot.dto.PerformanceStats;
import com.capitalot.dto.PortfolioPerformanceDto;
import com.capitalot.dto.PricePointDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.PortfolioHistory;
import com.capitalot.model.PortfolioStock;
import com.capitalot.model.User;
import com.capitalot.repository.PortfolioHistoryRepository;
import com.capitalot.repository.PortfolioStockRepository;
import com.capitalot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<PortfolioStock> stocks = portfolioStockRepository.findByUserId(user.getId());
        
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
    
    public List<PortfolioPerformanceDto> getPerformanceHistory(String email, String period) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDateTime startDate = getStartDateFromPeriod(period);
        List<PortfolioHistory> history = portfolioHistoryRepository
            .findByUserEmailAndTimestampAfter(email, startDate);
        
        if (history.isEmpty()) {
            List<PortfolioStock> stocks = portfolioStockRepository.findByUserId(user.getId());
            if (stocks.isEmpty()) {
                return new ArrayList<>();
            }
            return generateMockPerformanceHistory(user.getId(), period);
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
    
    private List<PortfolioPerformanceDto> generateMockPerformanceHistory(Long userId, String period) {
        List<PortfolioPerformanceDto> points = new ArrayList<>();
        LocalDateTime startDate = getStartDateFromPeriod(period);
        LocalDateTime now = LocalDateTime.now();
        
        List<PortfolioStock> stocks = portfolioStockRepository.findByUserId(userId);
        
        double currentValue = 0;
        for (PortfolioStock ps : stocks) {
            StockPriceResponse priceResponse = stockPriceService.getStockPrice(ps.getStock().getSymbol());
            currentValue += priceResponse.getCurrentPrice().doubleValue() * ps.getQuantity().doubleValue();
        }
        
        double totalInvested = 0;
        for (PortfolioStock ps : stocks) {
            totalInvested += ps.getPurchasePrice().doubleValue() * ps.getQuantity().doubleValue();
        }
        
        if (currentValue == 0 || totalInvested == 0) {
            return new ArrayList<>();
        }
        
        int dataPoints = getDataPointsForPeriod(period);
        long totalMinutes = java.time.Duration.between(startDate, now).toMinutes();
        long intervalMinutes = Math.max(1, totalMinutes / dataPoints);
        
        double startValue = totalInvested;
        double endValue = currentValue;
        double valueRange = endValue - startValue;
        
        for (int i = 0; i < dataPoints; i++) {
            LocalDateTime timestamp = startDate.plusMinutes(intervalMinutes * i);
            
            double progress = (double) i / Math.max(1, dataPoints - 1);
            double baseValue = startValue + (valueRange * progress);
            double variation = (random.nextDouble() - 0.5) * (Math.abs(valueRange) * 0.1);
            double value = baseValue + variation;
            
            double gainLoss = value - totalInvested;
            
            points.add(PortfolioPerformanceDto.builder()
                .timestamp(timestamp)
                .totalValue(value)
                .gainLoss(gainLoss)
                .build());
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

    public List<PortfolioPerformanceDto> getDynamicPortfolioChart(String email, String period, List<Long> portfolioIds) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<PortfolioStock> stocks;
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            stocks = portfolioStockRepository.findByUserId(user.getId());
        } else {
            stocks = portfolioStockRepository.findByPortfolioIdInAndUserId(portfolioIds, user.getId());
        }

        if (stocks.isEmpty()) return new ArrayList<>();

        // For 1D: use full LocalDateTime keys to preserve intraday granularity
        if ("1D".equals(period)) {
            return buildIntradayChart(stocks, period);
        }
        return buildDailyChart(stocks, period);
    }

    private List<PortfolioPerformanceDto> buildIntradayChart(List<PortfolioStock> stocks, String period) {
        Map<String, TreeMap<LocalDateTime, Double>> symbolPriceMaps = new HashMap<>();
        for (PortfolioStock ps : stocks) {
            String symbol = ps.getStock().getSymbol();
            if (!symbolPriceMaps.containsKey(symbol)) {
                List<PricePointDto> history = stockPriceService.getStockHistory(symbol, period);
                TreeMap<LocalDateTime, Double> priceMap = new TreeMap<>();
                for (PricePointDto p : history) {
                    if (p.getPrice() != null && p.getPrice() > 0) {
                        priceMap.put(p.getTimestamp(), p.getPrice());
                    }
                }
                symbolPriceMaps.put(symbol, priceMap);
                log.info("Loaded {} intraday price points for symbol {}", priceMap.size(), symbol);
            }
        }

        TreeSet<LocalDateTime> allTimestamps = new TreeSet<>();
        for (TreeMap<LocalDateTime, Double> priceMap : symbolPriceMaps.values()) {
            allTimestamps.addAll(priceMap.keySet());
        }

        if (allTimestamps.isEmpty()) return new ArrayList<>();

        List<PortfolioPerformanceDto> result = new ArrayList<>();
        for (LocalDateTime ts : allTimestamps) {
            double totalValue = 0.0;
            boolean hasAnyStock = false;
            LocalDate tsDate = ts.toLocalDate();

            for (PortfolioStock ps : stocks) {
                // Skip stocks sold before this timestamp
                if (ps.getSaleDate() != null && !ps.getSaleDate().toLocalDate().isAfter(tsDate)) continue;

                String symbol = ps.getStock().getSymbol();
                TreeMap<LocalDateTime, Double> priceMap = symbolPriceMaps.get(symbol);
                Map.Entry<LocalDateTime, Double> entry = priceMap.floorEntry(ts);
                if (entry != null && entry.getValue() > 0) {
                    totalValue += ps.getQuantity().doubleValue() * entry.getValue();
                    hasAnyStock = true;
                }
            }

            if (hasAnyStock) {
                result.add(PortfolioPerformanceDto.builder()
                    .timestamp(ts)
                    .totalValue(totalValue)
                    .build());
            }
        }

        log.info("Generated {} intraday chart points for period {}", result.size(), period);
        return result;
    }

    private List<PortfolioPerformanceDto> buildDailyChart(List<PortfolioStock> stocks, String period) {
        Map<String, TreeMap<LocalDate, Double>> symbolPriceMaps = new HashMap<>();
        for (PortfolioStock ps : stocks) {
            String symbol = ps.getStock().getSymbol();
            if (!symbolPriceMaps.containsKey(symbol)) {
                List<PricePointDto> history = stockPriceService.getStockHistory(symbol, period);
                TreeMap<LocalDate, Double> priceMap = new TreeMap<>();
                for (PricePointDto p : history) {
                    if (p.getPrice() != null && p.getPrice() > 0) {
                        priceMap.put(p.getTimestamp().toLocalDate(), p.getPrice());
                    }
                }
                symbolPriceMaps.put(symbol, priceMap);
                log.info("Loaded {} price points for symbol {}", priceMap.size(), symbol);
            }
        }

        TreeSet<LocalDate> allDates = new TreeSet<>();
        for (TreeMap<LocalDate, Double> priceMap : symbolPriceMaps.values()) {
            allDates.addAll(priceMap.keySet());
        }

        if (allDates.isEmpty()) return new ArrayList<>();

        List<PortfolioPerformanceDto> result = new ArrayList<>();
        for (LocalDate date : allDates) {
            double totalValue = 0.0;
            boolean hasAnyStock = false;

            for (PortfolioStock ps : stocks) {
                // Skip stocks sold before this date
                if (ps.getSaleDate() != null && !ps.getSaleDate().toLocalDate().isAfter(date)) continue;

                String symbol = ps.getStock().getSymbol();
                TreeMap<LocalDate, Double> priceMap = symbolPriceMaps.get(symbol);
                Map.Entry<LocalDate, Double> entry = priceMap.floorEntry(date);
                if (entry != null && entry.getValue() > 0) {
                    totalValue += ps.getQuantity().doubleValue() * entry.getValue();
                    hasAnyStock = true;
                }
            }

            if (hasAnyStock) {
                result.add(PortfolioPerformanceDto.builder()
                    .timestamp(date.atTime(16, 0))
                    .totalValue(totalValue)
                    .build());
            }
        }

        log.info("Generated {} chart points for period {}", result.size(), period);
        return result;
    }
}
