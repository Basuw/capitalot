package com.capitalot.service;

import com.capitalot.dto.PricePointDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.dto.alphavantage.AlphaVantageQuoteResponse;
import com.capitalot.dto.alphavantage.AlphaVantageTimeSeriesResponse;
import com.capitalot.model.Stock;
import com.capitalot.model.StockPriceHistory;
import com.capitalot.repository.StockPriceHistoryRepository;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceService {
    
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final AlphaVantageService alphaVantageService;
    private final Map<String, BigDecimal> priceCache = new HashMap<>();
    private final Random random = new Random();
    
    public StockPriceResponse getStockPrice(String symbol) {
        // Try to get real data from Alpha Vantage
        var quoteOpt = alphaVantageService.getQuote(symbol);
        
        if (quoteOpt.isPresent()) {
            AlphaVantageQuoteResponse.GlobalQuote quote = quoteOpt.get().getGlobalQuote();
            
            try {
                BigDecimal currentPrice = new BigDecimal(quote.getPrice());
                BigDecimal openPrice = new BigDecimal(quote.getOpen());
                BigDecimal highPrice = new BigDecimal(quote.getHigh());
                BigDecimal lowPrice = new BigDecimal(quote.getLow());
                BigDecimal previousClose = new BigDecimal(quote.getPreviousClose());
                
                // Parse change percent (remove % sign)
                String changePercentStr = quote.getChangePercent().replace("%", "");
                BigDecimal changePercent = new BigDecimal(changePercentStr);
                
                Long volume = Long.parseLong(quote.getVolume());
                
                log.info("Successfully fetched real price for {}: ${}", symbol, currentPrice);
                
                return StockPriceResponse.builder()
                    .symbol(symbol)
                    .currentPrice(currentPrice)
                    .openPrice(openPrice)
                    .highPrice(highPrice)
                    .lowPrice(lowPrice)
                    .previousClose(previousClose)
                    .changePercent(changePercent)
                    .volume(volume)
                    .currency("USD")
                    .build();
            } catch (NumberFormatException e) {
                log.warn("Failed to parse Alpha Vantage response for {}, falling back to mock data", symbol, e);
            }
        }
        
        // Fallback to mock data if Alpha Vantage fails
        log.info("Using mock data for symbol: {}", symbol);
        return generateMockPrice(symbol);
    }
    
    private StockPriceResponse generateMockPrice(String symbol) {
        BigDecimal basePrice = priceCache.computeIfAbsent(symbol, 
            s -> BigDecimal.valueOf(50 + random.nextDouble() * 450));
        
        BigDecimal variation = BigDecimal.valueOf(-5 + random.nextDouble() * 10);
        BigDecimal currentPrice = basePrice.add(variation);
        BigDecimal openPrice = basePrice;
        BigDecimal highPrice = currentPrice.max(basePrice).add(BigDecimal.valueOf(random.nextDouble() * 5));
        BigDecimal lowPrice = currentPrice.min(basePrice).subtract(BigDecimal.valueOf(random.nextDouble() * 5));
        BigDecimal changePercent = variation.divide(basePrice, 4, RoundingMode.HALF_UP)
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
        
        // Try to get real data from Alpha Vantage
        if (shouldUseIntradayData(period)) {
            var intradayOpt = alphaVantageService.getIntradayTimeSeries(symbol);
            if (intradayOpt.isPresent()) {
                return convertIntradayToHistory(intradayOpt.get(), period);
            }
        } else {
            var dailyOpt = alphaVantageService.getDailyTimeSeries(symbol);
            if (dailyOpt.isPresent()) {
                return convertDailyToHistory(dailyOpt.get(), period);
            }
        }
        
        // Fallback to database or mock data
        LocalDateTime startDate = getStartDateFromPeriod(period);
        List<StockPriceHistory> history = stockPriceHistoryRepository
            .findByStockIdAndTimestampAfter(stock.getId(), startDate);
        
        if (history.isEmpty()) {
            log.info("Using mock history data for symbol: {}", symbol);
            return generateMockHistory(symbol, period);
        }
        
        return history.stream()
            .map(h -> PricePointDto.builder()
                .timestamp(h.getTimestamp())
                .price(h.getPrice())
                .build())
            .toList();
    }
    
    private boolean shouldUseIntradayData(String period) {
        return period.equals("1D") || period.equals("1W");
    }
    
    private List<PricePointDto> convertIntradayToHistory(AlphaVantageTimeSeriesResponse response, String period) {
        List<PricePointDto> points = new ArrayList<>();
        LocalDateTime cutoff = getStartDateFromPeriod(period);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        response.getTimeSeriesIntraday().forEach((timestamp, data) -> {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);
                if (dateTime.isAfter(cutoff)) {
                    double price = Double.parseDouble(data.getClose());
                    points.add(PricePointDto.builder()
                        .timestamp(dateTime)
                        .price(price)
                        .build());
                }
            } catch (Exception e) {
                log.warn("Failed to parse intraday data point: {}", timestamp, e);
            }
        });
        
        points.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        log.info("Converted {} intraday data points", points.size());
        return points;
    }
    
    private List<PricePointDto> convertDailyToHistory(AlphaVantageTimeSeriesResponse response, String period) {
        List<PricePointDto> points = new ArrayList<>();
        LocalDateTime cutoff = getStartDateFromPeriod(period);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        response.getTimeSeriesDaily().forEach((dateStr, data) -> {
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                LocalDateTime dateTime = date.atStartOfDay();
                if (dateTime.isAfter(cutoff)) {
                    double price = Double.parseDouble(data.getClose());
                    points.add(PricePointDto.builder()
                        .timestamp(dateTime)
                        .price(price)
                        .build());
                }
            } catch (Exception e) {
                log.warn("Failed to parse daily data point: {}", dateStr, e);
            }
        });
        
        points.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        log.info("Converted {} daily data points", points.size());
        return points;
    }
    
    public List<PricePointDto> getPriceHistory(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
            .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        return generateMockHistory(symbol, "1Y");
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
    
    /**
     * Get historical price for a specific date
     * @param symbol Stock symbol
     * @param date Target date
     * @return Price at the specified date, or current price if not available
     */
    public BigDecimal getHistoricalPrice(String symbol, LocalDateTime date) {
        log.info("Fetching historical price for {} at {}", symbol, date);
        
        // Try to get from Alpha Vantage daily time series
        var dailyOpt = alphaVantageService.getDailyTimeSeries(symbol);
        if (dailyOpt.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dateStr = date.toLocalDate().format(formatter);
            
            var timeSeries = dailyOpt.get().getTimeSeriesDaily();
            if (timeSeries != null && timeSeries.containsKey(dateStr)) {
                try {
                    String closePrice = timeSeries.get(dateStr).getClose();
                    BigDecimal price = new BigDecimal(closePrice);
                    log.info("Found historical price for {} on {}: ${}", symbol, dateStr, price);
                    return price;
                } catch (Exception e) {
                    log.warn("Failed to parse historical price for {} on {}", symbol, dateStr, e);
                }
            } else {
                log.warn("No data found for {} on {}, trying nearby dates", symbol, dateStr);
                // Try to find the closest previous date (in case of weekend/holiday)
                for (int i = 1; i <= 7; i++) {
                    String altDateStr = date.minusDays(i).toLocalDate().format(formatter);
                    if (timeSeries != null && timeSeries.containsKey(altDateStr)) {
                        try {
                            String closePrice = timeSeries.get(altDateStr).getClose();
                            BigDecimal price = new BigDecimal(closePrice);
                            log.info("Found historical price for {} on {} (used {}): ${}", symbol, dateStr, altDateStr, price);
                            return price;
                        } catch (Exception e) {
                            log.warn("Failed to parse historical price for {} on {}", symbol, altDateStr, e);
                        }
                    }
                }
            }
        }
        
        // Fallback: check database
        Stock stock = stockRepository.findBySymbol(symbol).orElse(null);
        if (stock != null) {
            List<StockPriceHistory> history = stockPriceHistoryRepository
                .findByStockIdAndTimestampAfter(stock.getId(), date.minusDays(7));
            
            if (!history.isEmpty()) {
                // Find closest price to the target date
                StockPriceHistory closestPrice = history.stream()
                    .min((a, b) -> {
                        long diffA = Math.abs(java.time.Duration.between(date, a.getTimestamp()).toMinutes());
                        long diffB = Math.abs(java.time.Duration.between(date, b.getTimestamp()).toMinutes());
                        return Long.compare(diffA, diffB);
                    })
                    .orElse(null);
                
                if (closestPrice != null) {
                    log.info("Found historical price from database for {} near {}: ${}", symbol, date, closestPrice.getPrice());
                    return BigDecimal.valueOf(closestPrice.getPrice());
                }
            }
        }
        
        // Last fallback: use current price
        log.warn("Could not find historical price for {} on {}, using current price", symbol, date);
        StockPriceResponse currentPriceResponse = getStockPrice(symbol);
        return currentPriceResponse.getCurrentPrice();
    }
}
