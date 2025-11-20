package com.capitalot.service;

import com.capitalot.model.Stock;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StockSearchService {
    
    private final StockRepository stockRepository;
    private final Random random = new Random();
    
    public List<Stock> searchStocks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return stockRepository.findPopularStocks();
        }
        return stockRepository.searchStocks(query);
    }
    
    public List<Stock> getPopularStocks() {
        return stockRepository.findPopularStocks();
    }
    
    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
    }
    
    public void initializePopularStocks() {
        if (stockRepository.count() == 0) {
            List<Stock> popularStocks = Arrays.asList(
                createStock("AAPL", "Apple Inc.", "NASDAQ", "Technology", "Consumer Electronics", true, "Apple designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide.", 0.96, 4.5),
                createStock("MSFT", "Microsoft Corporation", "NASDAQ", "Technology", "Software", true, "Microsoft develops, licenses, and supports software, services, devices, and solutions worldwide.", 0.75, 3.2),
                createStock("GOOGL", "Alphabet Inc.", "NASDAQ", "Technology", "Internet Services", true, "Alphabet provides various products and services in the United States, Europe, the Middle East, Africa, the Asia-Pacific, Canada, and Latin America.", 0.0, 6.8),
                createStock("AMZN", "Amazon.com Inc.", "NASDAQ", "Consumer Cyclical", "Internet Retail", true, "Amazon engages in the retail sale of consumer products and subscriptions in North America and internationally.", 0.0, 5.5),
                createStock("TSLA", "Tesla Inc.", "NASDAQ", "Consumer Cyclical", "Auto Manufacturers", true, "Tesla designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems.", 0.0, 8.2),
                createStock("NVDA", "NVIDIA Corporation", "NASDAQ", "Technology", "Semiconductors", true, "NVIDIA provides graphics, and compute and networking solutions in the United States, Taiwan, China, and internationally.", 0.03, 7.1),
                createStock("META", "Meta Platforms Inc.", "NASDAQ", "Technology", "Internet Services", true, "Meta Platforms builds technologies that help people connect, find communities, and grow businesses.", 0.0, 6.5),
                createStock("BRK.B", "Berkshire Hathaway Inc.", "NYSE", "Financial", "Insurance", true, "Berkshire Hathaway provides insurance and reinsurance, utilities and energy, freight rail transportation, and other services.", 0.0, 4.0),
                createStock("V", "Visa Inc.", "NYSE", "Financial", "Credit Services", true, "Visa operates as a payments technology company worldwide.", 0.87, 3.8),
                createStock("JPM", "JPMorgan Chase & Co.", "NYSE", "Financial", "Banks", true, "JPMorgan Chase provides various financial services worldwide.", 2.62, 5.2)
            );
            stockRepository.saveAll(popularStocks);
        }
    }
    
    private Stock createStock(String symbol, String name, String exchange, String sector, String industry, boolean isPopular, String description, double annualDividend, double risk) {
        double longPct = 50.0 + random.nextDouble() * 40.0;
        double shortPct = 100.0 - longPct;
        
        return Stock.builder()
            .symbol(symbol)
            .name(name)
            .exchange(exchange)
            .currency("USD")
            .sector(sector)
            .industry(industry)
            .isPopular(isPopular)
            .description(description)
            .annualDividend(annualDividend)
            .risk(risk)
            .longPercentage(longPct)
            .shortPercentage(shortPct)
            .marketScore(70.0 + random.nextDouble() * 30.0)
            .build();
    }
}
