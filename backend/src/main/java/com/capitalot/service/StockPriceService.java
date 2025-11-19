package com.capitalot.service;

import com.capitalot.dto.PricePointDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.Stock;
import com.capitalot.model.StockPriceHistory;
import com.capitalot.repository.StockPriceHistoryRepository;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StockPriceService {
    
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final Map<String, BigDecimal> priceCache = new HashMap<>();
    private final Random random = new Random();
    
    public StockPriceResponse getStockPrice(String symbol) {
        BigDecimal basePrice = priceCache.computeIfAbsent(symbol, 
            s -> BigDecimal.valueOf(50 + random.nextDouble() * 450));
        
        BigDecimal variation = BigDecimal.valueOf(-5 + random.nextDouble() * 10);
        BigDecimal currentPrice = basePrice.add(variation);
        BigDecimal openPrice = basePrice;
        BigDecimal highPrice = currentPrice.max(basePrice).add(BigDecimal.valueOf(random.nextDouble() * 5));
        BigDecimal lowPrice = currentPrice.min(basePrice).subtract(BigDecimal.valueOf(random.nextDouble() * 5));
        BigDecimal changePercent = variation.divide(basePrice, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        return StockPriceResponse.builder()
            .symbol(symbol)
            .currentPrice(currentPrice)
            .openPrice(openPrice)
            .highPrice(highPrice)
            .lowPrice(lowPrice)
            .previousClose(basePrice)
            .changePercent(changePercent)
            .volume(1000000L + random.nextInt(9000000))
            .currency("USD")
            .build();
    }
    
    public List<PricePointDto> getStockHistory(String symbol, String period) {
        Stock stock = stockRepository.findBySymbol(symbol)
            .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        LocalDateTime startDate = getStartDateFromPeriod(period);
        List<StockPriceHistory> history = stockPriceHistoryRepository
            .findByStockIdAndTimestampAfter(stock.getId(), startDate);
        
        if (history.isEmpty()) {
            return generateMockHistory(symbol, period);
        }
        
        return history.stream()
            .map(h -> PricePointDto.builder()
                .timestamp(h.getTimestamp())
                .price(h.getPrice())
                .build())
            .toList();
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
    
    private List<PricePointDto> generateMockHistory(String symbol, String period) {
        List<PricePointDto> points = new ArrayList<>();
        LocalDateTime startDate = getStartDateFromPeriod(period);
        LocalDateTime now = LocalDateTime.now();
        
        double basePrice = 50 + random.nextDouble() * 450;
        int dataPoints = getDataPointsForPeriod(period);
        long totalMinutes = java.time.Duration.between(startDate, now).toMinutes();
        long intervalMinutes = totalMinutes / dataPoints;
        
        for (int i = 0; i < dataPoints; i++) {
            LocalDateTime timestamp = startDate.plusMinutes(intervalMinutes * i);
            double variation = (random.nextDouble() - 0.5) * 20;
            double price = basePrice + variation;
            
            points.add(PricePointDto.builder()
                .timestamp(timestamp)
                .price(price)
                .build());
            
            basePrice = price;
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
