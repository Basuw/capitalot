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
                createStock("AAPL", "Apple Inc.", "NASDAQ", "Technology", "Consumer Electronics", true, 180.0),
                createStock("MSFT", "Microsoft Corporation", "NASDAQ", "Technology", "Software", true, 420.0),
                createStock("GOOGL", "Alphabet Inc.", "NASDAQ", "Technology", "Internet Services", true, 142.0),
                createStock("AMZN", "Amazon.com Inc.", "NASDAQ", "Consumer Cyclical", "Internet Retail", true, 175.0),
                createStock("TSLA", "Tesla Inc.", "NASDAQ", "Consumer Cyclical", "Auto Manufacturers", true, 242.0),
                createStock("NVDA", "NVIDIA Corporation", "NASDAQ", "Technology", "Semiconductors", true, 495.0),
                createStock("META", "Meta Platforms Inc.", "NASDAQ", "Technology", "Internet Services", true, 520.0),
                createStock("BRK.B", "Berkshire Hathaway Inc.", "NYSE", "Financial", "Insurance", true, 445.0),
                createStock("V", "Visa Inc.", "NYSE", "Financial", "Credit Services", true, 280.0),
                createStock("JPM", "JPMorgan Chase & Co.", "NYSE", "Financial", "Banks", true, 195.0)
            );
            stockRepository.saveAll(popularStocks);
        }
    }
    
    private Stock createStock(String symbol, String name, String exchange, String sector, String industry, boolean isPopular, double basePrice) {
        return Stock.builder()
            .symbol(symbol)
            .name(name)
            .exchange(exchange)
            .currency("USD")
            .sector(sector)
            .industry(industry)
            .isPopular(isPopular)
            .marketScore(70.0 + random.nextDouble() * 30.0)
            .build();
    }
}
