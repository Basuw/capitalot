package com.capitalot.service;

import com.capitalot.dto.PricePointDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceChartResponse;
import com.capitalot.model.Stock;
import com.capitalot.model.StockPriceHistory;
import com.capitalot.repository.StockPriceHistoryRepository;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final YahooFinanceService yahooFinanceService;
    private final Map<String, BigDecimal> priceCache = new HashMap<>();
    private final Random random = new Random();
    
    public StockPriceResponse getStockPrice(String symbol) {
        var chartOpt = yahooFinanceService.getChartData(symbol, "1d", "1m");
        
        if (chartOpt.isPresent()) {
            YahooFinanceChartResponse response = chartOpt.get();
            YahooFinanceChartResponse.Result result = response.getChart().getResult().get(0);
            
            try {
                YahooFinanceChartResponse.Meta meta = result.getMeta();
                Double currentPrice = meta.getRegularMarketPrice();
                Long marketTime = meta.getRegularMarketTime();
                
                if (currentPrice == null) {
                    YahooFinanceChartResponse.Quote quote = result.getIndicators().getQuote().get(0);
                    List<Double> closePrices = quote.getClose();
                    if (closePrices != null && !closePrices.isEmpty()) {
                        int lastIndex = closePrices.size() - 1;
                        while (lastIndex >= 0 && closePrices.get(lastIndex) == null) {
                            lastIndex--;
                        }
                        if (lastIndex >= 0) {
                            currentPrice = closePrices.get(lastIndex);
                            if (result.getTimestamp() != null && result.getTimestamp().size() > lastIndex) {
                                marketTime = result.getTimestamp().get(lastIndex);
                            }
                        }
                    }
                }
                
                if (currentPrice != null) {
                    LocalDateTime dateTime = Instant.ofEpochSecond(marketTime != null ? marketTime : Instant.now().getEpochSecond())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                    
                    log.info("[DEBUG] Fetched price for {}: ${} at date: {}", symbol, currentPrice, dateTime);
                    
                    Double previousClose = meta.getPreviousClose() != null ? meta.getPreviousClose() : currentPrice;
                    BigDecimal changePercent = BigDecimal.valueOf((currentPrice - previousClose) / previousClose * 100)
                        .setScale(2, RoundingMode.HALF_UP);
                    
                    return StockPriceResponse.builder()
                        .symbol(symbol)
                        .currentPrice(BigDecimal.valueOf(currentPrice))
                        .previousClose(BigDecimal.valueOf(previousClose))
                        .changePercent(changePercent)
                        .currency(meta.getCurrency() != null ? meta.getCurrency() : "USD")
                        .lastUpdated(dateTime)
                        .build();
                }
            } catch (Exception e) {
                log.warn("Failed to parse Yahoo Finance chart response for {}, trying quoteSummary", symbol, e);
            }
        }
        
        // Fallback to Quote Summary for current price
        var summaryOpt = yahooFinanceService.getQuoteSummary(symbol);
        if (summaryOpt.isPresent()) {
            var summary = summaryOpt.get();
            if (summary.getQuoteSummary() != null && !summary.getQuoteSummary().getResult().isEmpty()) {
                var financialData = summary.getQuoteSummary().getResult().get(0).getFinancialData();
                if (financialData != null && financialData.getCurrentPrice() != null && financialData.getCurrentPrice().getRaw() != null) {
                    Double currentPrice = financialData.getCurrentPrice().getRaw();
                    log.info("[DEBUG] Fetched price for {} from quoteSummary: ${} at now: {}", symbol, currentPrice, LocalDateTime.now());
                    
                    return StockPriceResponse.builder()
                        .symbol(symbol)
                        .currentPrice(BigDecimal.valueOf(currentPrice))
                        .previousClose(BigDecimal.valueOf(currentPrice)) // approximation
                        .changePercent(BigDecimal.ZERO)
                        .currency("USD")
                        .build();
                }
            }
        }
        
        log.warn("Using mock data for symbol: {} - Could not fetch real price", symbol);
        return generateMockPrice(symbol);
    }
    
    private Double getMaxPrice(List<Double> prices) {
        return prices.stream()
            .filter(p -> p != null)
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);
    }
    
    private Double getMinPrice(List<Double> prices) {
        return prices.stream()
            .filter(p -> p != null)
            .mapToDouble(Double::doubleValue)
            .min()
            .orElse(0.0);
    }
    
    private StockPriceResponse generateMockPrice(String symbol) {
        BigDecimal basePrice = priceCache.computeIfAbsent(symbol, 
            s -> BigDecimal.valueOf(50 + random.nextDouble() * 450));
        
        double maxVariation = basePrice.doubleValue() * 0.02;
        BigDecimal variation = BigDecimal.valueOf(-maxVariation + random.nextDouble() * (maxVariation * 2));
        BigDecimal currentPrice = basePrice.add(variation);
        if (currentPrice.doubleValue() <= 0) {
            currentPrice = basePrice;
        }
        
        BigDecimal openPrice = basePrice;
        BigDecimal highPrice = currentPrice.max(basePrice).add(BigDecimal.valueOf(random.nextDouble() * 2));
        BigDecimal lowPrice = currentPrice.min(basePrice).subtract(BigDecimal.valueOf(random.nextDouble() * 2));
        if (lowPrice.doubleValue() <= 0) {
            lowPrice = currentPrice.multiply(BigDecimal.valueOf(0.95));
        }
        
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
        
        String range = mapPeriodToYahooRange(period);
        String interval = shouldUseIntradayData(period) ? "1m" : "1d";
        
        var chartOpt = yahooFinanceService.getChartData(symbol, range, interval);
        if (chartOpt.isPresent()) {
            return convertYahooChartToHistory(chartOpt.get());
        }
        
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
    
    private String mapPeriodToYahooRange(String period) {
        return switch (period) {
            case "1D" -> "1d";
            case "1W" -> "5d";
            case "1M" -> "1mo";
            case "3M" -> "3mo";
            case "6M" -> "6mo";
            case "1Y" -> "1y";
            case "5Y" -> "5y";
            default -> "1mo";
        };
    }
    
    private List<PricePointDto> convertYahooChartToHistory(YahooFinanceChartResponse response) {
        List<PricePointDto> points = new ArrayList<>();
        YahooFinanceChartResponse.Result result = response.getChart().getResult().get(0);
        List<Long> timestamps = result.getTimestamp();
        YahooFinanceChartResponse.Quote quote = result.getIndicators().getQuote().get(0);
        List<Double> closePrice = quote.getClose();
        
        if (timestamps == null || closePrice == null || timestamps.size() != closePrice.size()) {
            log.warn("Mismatched timestamps and price data");
            return points;
        }
        
        for (int i = 0; i < timestamps.size(); i++) {
            try {
                Long timestamp = timestamps.get(i);
                Double price = closePrice.get(i);
                
                if (timestamp != null && price != null) {
                    LocalDateTime dateTime = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                    
                    points.add(PricePointDto.builder()
                        .timestamp(dateTime)
                        .price(price)
                        .build());
                }
            } catch (Exception e) {
                log.warn("Failed to parse Yahoo Finance data point at index {}", i, e);
            }
        }
        
        points.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        log.info("Converted {} Yahoo Finance data points", points.size());
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
        
        BigDecimal basePrice = priceCache.computeIfAbsent(symbol, 
            s -> BigDecimal.valueOf(50 + random.nextDouble() * 450));
        
        int dataPoints = getDataPointsForPeriod(period);
        long totalMinutes = java.time.Duration.between(startDate, now).toMinutes();
        long intervalMinutes = Math.max(1, totalMinutes / dataPoints);
        
        double startPrice = basePrice.doubleValue() * (0.85 + random.nextDouble() * 0.3);
        double endPrice = basePrice.doubleValue();
        
        for (int i = 0; i < dataPoints; i++) {
            LocalDateTime timestamp = startDate.plusMinutes(intervalMinutes * i);
            
            double progress = (double) i / Math.max(1, dataPoints - 1);
            double baseValue = startPrice + ((endPrice - startPrice) * progress);
            
            double maxVariation = basePrice.doubleValue() * 0.03;
            double variation = (-maxVariation + random.nextDouble() * (maxVariation * 2)) * (1.0 - (progress * 0.2));
            double price = baseValue + variation;
            
            if (price <= 0) {
                price = baseValue;
            }
            
            points.add(PricePointDto.builder()
                .timestamp(timestamp)
                .price(price)
                .build());
        }
        
        return points;
    }
    
    private boolean shouldUseIntradayData(String period) {
        return period.equals("1D") || period.equals("1W");
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
    
    public BigDecimal getHistoricalPrice(String symbol, LocalDateTime date) {
        log.info("Fetching historical price for {} at {}", symbol, date);
        
        // Convert LocalDateTime to Epoch seconds (start and end of the day)
        long period1 = date.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long period2 = date.toLocalDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        
        var chartOpt = yahooFinanceService.getChartDataForPeriod(symbol, period1, period2, "1d");
        
        if (chartOpt.isPresent()) {
            YahooFinanceChartResponse response = chartOpt.get();
            YahooFinanceChartResponse.Result result = response.getChart().getResult().get(0);
            List<Long> timestamps = result.getTimestamp();
            YahooFinanceChartResponse.Quote quote = result.getIndicators().getQuote().get(0);
            List<Double> closePrice = quote.getClose();
            
            if (closePrice != null && !closePrice.isEmpty()) {
                // Find the closest price to the requested date if multiple points returned
                int index = 0;
                long targetEpoch = date.atZone(ZoneId.systemDefault()).toEpochSecond();
                long minDiff = Long.MAX_VALUE;
                
                for (int i = 0; i < timestamps.size(); i++) {
                    if (closePrice.get(i) != null) {
                        long diff = Math.abs(timestamps.get(i) - targetEpoch);
                        if (diff < minDiff) {
                            minDiff = diff;
                            index = i;
                        }
                    }
                }
                
                if (closePrice.get(index) != null) {
                    BigDecimal price = BigDecimal.valueOf(closePrice.get(index));
                    log.info("Found accurate historical price for {} on {}: ${}", symbol, date.toLocalDate(), price);
                    return price;
                }
            }
        }
        
        // Fallback to broader range if specific day failed
        String range = "1y";
        var broadChartOpt = yahooFinanceService.getChartData(symbol, range, "1d");
        
        if (broadChartOpt.isPresent()) {
            YahooFinanceChartResponse response = broadChartOpt.get();
            YahooFinanceChartResponse.Result result = response.getChart().getResult().get(0);
            List<Long> timestamps = result.getTimestamp();
            YahooFinanceChartResponse.Quote quote = result.getIndicators().getQuote().get(0);
            List<Double> closePrice = quote.getClose();
            
            long targetEpoch = date.atZone(ZoneId.systemDefault()).toEpochSecond();
            
            for (int i = 0; i < timestamps.size(); i++) {
                if (closePrice.get(i) != null && Math.abs(timestamps.get(i) - targetEpoch) < 86400) {
                    BigDecimal price = BigDecimal.valueOf(closePrice.get(i));
                    log.info("Found historical price from broad range for {} on {}: ${}", symbol, date.toLocalDate(), price);
                    return price;
                }
            }
        }
        
        Stock stock = stockRepository.findBySymbol(symbol).orElse(null);
        if (stock != null) {
            List<StockPriceHistory> history = stockPriceHistoryRepository
                .findByStockIdAndTimestampAfter(stock.getId(), date.minusDays(7));
            
            if (!history.isEmpty()) {
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
        
        log.warn("Could not find historical price for {} on {}, using current price", symbol, date);
        StockPriceResponse currentPriceResponse = getStockPrice(symbol);
        return currentPriceResponse.getCurrentPrice();
    }
}
