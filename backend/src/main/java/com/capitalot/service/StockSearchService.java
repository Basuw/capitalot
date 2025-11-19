package com.capitalot.service;

import com.capitalot.model.Stock;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockSearchService {
    
    private final StockRepository stockRepository;
    
    public List<Stock> searchStocks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return stockRepository.findPopularStocks();
        }
        return stockRepository.searchStocks(query);
    }
    
    public List<Stock> getPopularStocks() {
        return stockRepository.findPopularStocks();
    }
    
    public void initializePopularStocks() {
        if (stockRepository.count() == 0) {
            List<Stock> popularStocks = Arrays.asList(
                createStock("AAPL", "Apple Inc.", "NASDAQ", "Technology", "Consumer Electronics", true),
                createStock("MSFT", "Microsoft Corporation", "NASDAQ", "Technology", "Software", true),
                createStock("GOOGL", "Alphabet Inc.", "NASDAQ", "Technology", "Internet Services", true),
                createStock("AMZN", "Amazon.com Inc.", "NASDAQ", "Consumer Cyclical", "Internet Retail", true),
                createStock("TSLA", "Tesla Inc.", "NASDAQ", "Consumer Cyclical", "Auto Manufacturers", true),
                createStock("NVDA", "NVIDIA Corporation", "NASDAQ", "Technology", "Semiconductors", true),
                createStock("META", "Meta Platforms Inc.", "NASDAQ", "Technology", "Internet Services", true),
                createStock("BRK.B", "Berkshire Hathaway Inc.", "NYSE", "Financial", "Insurance", true),
                createStock("V", "Visa Inc.", "NYSE", "Financial", "Credit Services", true),
                createStock("JPM", "JPMorgan Chase & Co.", "NYSE", "Financial", "Banks", true)
            );
            stockRepository.saveAll(popularStocks);
        }
    }
    
    private Stock createStock(String symbol, String name, String exchange, String sector, String industry, boolean isPopular) {
        return Stock.builder()
            .symbol(symbol)
            .name(name)
            .exchange(exchange)
            .currency("USD")
            .sector(sector)
            .industry(industry)
            .isPopular(isPopular)
            .build();
    }
}
