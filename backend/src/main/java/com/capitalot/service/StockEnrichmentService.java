package com.capitalot.service;

import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockEnrichmentService {
    
    private final StockPriceService stockPriceService;
    
    /**
     * Enrichit un stock avec les vraies données de prix depuis Alpha Vantage
     */
    public Stock enrichStockWithRealPrice(Stock stock) {
        try {
            StockPriceResponse priceData = stockPriceService.getStockPrice(stock.getSymbol());
            
            stock.setCurrentPrice(priceData.getCurrentPrice().doubleValue());
            stock.setOpenPrice(priceData.getOpenPrice() != null ? priceData.getOpenPrice().doubleValue() : null);
            stock.setHighPrice(priceData.getHighPrice() != null ? priceData.getHighPrice().doubleValue() : null);
            stock.setLowPrice(priceData.getLowPrice() != null ? priceData.getLowPrice().doubleValue() : null);
            stock.setPreviousClose(priceData.getPreviousClose() != null ? priceData.getPreviousClose().doubleValue() : null);
            stock.setVolume(priceData.getVolume());
            stock.setDailyChange(priceData.getCurrentPrice().subtract(priceData.getPreviousClose()).doubleValue());
            stock.setDailyChangePercentage(priceData.getChangePercent().doubleValue());
            
            log.debug("Enriched stock {} with real price: ${}", stock.getSymbol(), stock.getCurrentPrice());
        } catch (Exception e) {
            log.warn("Failed to enrich stock {} with real price, using calculated values", stock.getSymbol(), e);
            // Les valeurs calculées dans @PostLoad seront utilisées
        }
        
        return stock;
    }
    
    /**
     * Enrichit une liste de stocks avec les vraies données de prix
     */
    public List<Stock> enrichStocksWithRealPrices(List<Stock> stocks) {
        return stocks.stream()
                .map(this::enrichStockWithRealPrice)
                .toList();
    }
}
