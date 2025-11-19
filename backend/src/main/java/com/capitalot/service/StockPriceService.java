package com.capitalot.service;

import com.capitalot.dto.StockPriceResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class StockPriceService {
    
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
}
