package com.capitalot.service;

import com.capitalot.dto.StockPriceResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceQuoteSummaryResponse;
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
    private final YahooFinanceService yahooFinanceService;
    
    /**
     * Enrichit un stock avec les vraies données de prix depuis Yahoo Finance
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
            
            // Enrich with additional details from Yahoo if missing
            if ("Unknown".equals(stock.getSector()) || "Unknown".equals(stock.getIndustry()) || stock.getDescription() == null || stock.getDescription().equals(stock.getName())) {
                enrichStockDetails(stock);
            }
            
            log.debug("Enriched stock {} with real price: ${}", stock.getSymbol(), stock.getCurrentPrice());
        } catch (Exception e) {
            log.warn("Failed to enrich stock {} with real price, using calculated values", stock.getSymbol(), e);
        }
        
        return stock;
    }

    private void enrichStockDetails(Stock stock) {
        yahooFinanceService.getQuoteSummary(stock.getSymbol()).ifPresent(summary -> {
            if (summary.getQuoteSummary() != null && summary.getQuoteSummary().getResult() != null && !summary.getQuoteSummary().getResult().isEmpty()) {
                YahooFinanceQuoteSummaryResponse.Result result = summary.getQuoteSummary().getResult().get(0);
                
                if (result.getAssetProfile() != null) {
                    YahooFinanceQuoteSummaryResponse.AssetProfile profile = result.getAssetProfile();
                    if (profile.getSector() != null) stock.setSector(profile.getSector());
                    if (profile.getIndustry() != null) stock.setIndustry(profile.getIndustry());
                    if (profile.getLongBusinessSummary() != null) stock.setDescription(profile.getLongBusinessSummary());
                }
                
                if (result.getCalendarEvents() != null && result.getCalendarEvents().getExDividendDate() != null) {
                    // This is just an example of what else we could enrich
                    log.debug("Found dividend date for {}: {}", stock.getSymbol(), result.getCalendarEvents().getExDividendDate().getFmt());
                }
            }
        });
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
