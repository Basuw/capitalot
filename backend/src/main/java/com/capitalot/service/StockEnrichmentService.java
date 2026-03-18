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
    
    public Stock enrichStockWithRealPrice(Stock stock) {
        if (stock == null) return null;
        
        try {
            StockPriceResponse priceData = stockPriceService.getStockPrice(stock.getSymbol());
            
            if (priceData != null && priceData.getCurrentPrice() != null) {
                stock.setCurrentPrice(priceData.getCurrentPrice().doubleValue());
                if (priceData.getOpenPrice() != null) stock.setOpenPrice(priceData.getOpenPrice().doubleValue());
                if (priceData.getHighPrice() != null) stock.setHighPrice(priceData.getHighPrice().doubleValue());
                if (priceData.getLowPrice() != null) stock.setLowPrice(priceData.getLowPrice().doubleValue());
                if (priceData.getPreviousClose() != null) stock.setPreviousClose(priceData.getPreviousClose().doubleValue());
                
                stock.setVolume(priceData.getVolume());
                
                if (priceData.getPreviousClose() != null) {
                    stock.setDailyChange(priceData.getCurrentPrice().subtract(priceData.getPreviousClose()).doubleValue());
                }
                
                if (priceData.getChangePercent() != null) {
                    stock.setDailyChangePercentage(priceData.getChangePercent().doubleValue());
                }
                
                stock.setLastPriceUpdate(priceData.getLastUpdated());
            }

            // Enrichissement des détails si "Unknown"
            if (stock.getSector() == null || "Unknown".equals(stock.getSector())) {
                enrichStockDetails(stock);
            }
        } catch (Exception e) {
            log.error("Error enriching stock {}: {}", stock.getSymbol(), e.getMessage());
            // On ne rebalance pas l'exception pour permettre l'affichage même sans prix frais
        }
        
        return stock;
    }

    private void enrichStockDetails(Stock stock) {
        try {
            yahooFinanceService.getQuoteSummary(stock.getSymbol()).ifPresent(summary -> {
                if (summary.getQuoteSummary() != null && summary.getQuoteSummary().getResult() != null && !summary.getQuoteSummary().getResult().isEmpty()) {
                    YahooFinanceQuoteSummaryResponse.Result result = summary.getQuoteSummary().getResult().get(0);
                    
                    if (result.getAssetProfile() != null) {
                        YahooFinanceQuoteSummaryResponse.AssetProfile profile = result.getAssetProfile();
                        if (profile.getSector() != null) stock.setSector(profile.getSector());
                        if (profile.getIndustry() != null) stock.setIndustry(profile.getIndustry());
                        if (profile.getLongBusinessSummary() != null) stock.setDescription(profile.getLongBusinessSummary());
                        
                        if (profile.getWebsite() != null && (stock.getLogoUrl() == null || stock.getLogoUrl().isEmpty())) {
                            String domain = profile.getWebsite()
                                    .replace("http://", "")
                                    .replace("https://", "")
                                    .replace("www.", "")
                                    .split("/")[0];
                            stock.setLogoUrl("https://logo.clearbit.com/" + domain);
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.warn("Could not enrich details for {}: {}", stock.getSymbol(), e.getMessage());
        }
    }
    
    public List<Stock> enrichStocksWithRealPrices(List<Stock> stocks) {
        if (stocks == null) return List.of();
        return stocks.stream()
                .map(this::enrichStockWithRealPrice)
                .toList();
    }
}
