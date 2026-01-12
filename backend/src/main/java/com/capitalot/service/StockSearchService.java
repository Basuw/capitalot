package com.capitalot.service;

import com.capitalot.dto.alphavantage.AlphaVantageSearchResponse;
import com.capitalot.model.Stock;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockSearchService {
    
    private final StockRepository stockRepository;
    private final StockEnrichmentService stockEnrichmentService;
    private final AlphaVantageService alphaVantageService;
    private final Random random = new Random();
    
    public List<Stock> searchStocks(String query) {
        List<Stock> stocks;
        if (query == null || query.trim().isEmpty()) {
            stocks = stockRepository.findPopularStocks();
        } else {
            // D'abord chercher dans la base de données locale
            stocks = stockRepository.searchStocks(query);
            
            // Si peu ou pas de résultats locaux, rechercher via Alpha Vantage API
            if (stocks.size() < 3) {
                log.info("Searching Alpha Vantage API for query: {}", query);
                var searchResults = alphaVantageService.searchSymbol(query);
                
                if (searchResults.isPresent() && searchResults.get().getBestMatches() != null) {
                    // Convertir les résultats de l'API en objets Stock
                    List<Stock> apiStocks = searchResults.get().getBestMatches().stream()
                            .filter(match -> "Equity".equalsIgnoreCase(match.getType()) || 
                                           "ETF".equalsIgnoreCase(match.getType()))
                            .map(this::convertSearchMatchToStock)
                            .collect(Collectors.toList());
                    
                    // Sauvegarder les nouveaux stocks dans la base de données
                    for (Stock apiStock : apiStocks) {
                        if (!stockRepository.existsBySymbol(apiStock.getSymbol())) {
                            stockRepository.save(apiStock);
                            stocks.add(apiStock);
                        }
                    }
                    
                    log.info("Found {} stocks from Alpha Vantage API", apiStocks.size());
                }
            }
        }
        
        // Enrichir avec les vrais prix depuis Alpha Vantage
        return stockEnrichmentService.enrichStocksWithRealPrices(stocks);
    }
    
    public List<Stock> getPopularStocks() {
        List<Stock> stocks = stockRepository.findPopularStocks();
        // Enrichir avec les vrais prix depuis Alpha Vantage
        return stockEnrichmentService.enrichStocksWithRealPrices(stocks);
    }
    
    public Stock getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        
        // Enrichir avec le vrai prix depuis Alpha Vantage
        return stockEnrichmentService.enrichStockWithRealPrice(stock);
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
            .stockType(com.capitalot.model.StockType.STOCK)
            .description(description)
            .annualDividend(annualDividend)
            .risk(risk)
            .longPercentage(longPct)
            .shortPercentage(shortPct)
            .marketScore(70.0 + random.nextDouble() * 30.0)
            .build();
    }
    
    private Stock convertSearchMatchToStock(AlphaVantageSearchResponse.SearchMatch match) {
        double longPct = 50.0 + random.nextDouble() * 40.0;
        double shortPct = 100.0 - longPct;
        
        return Stock.builder()
                .symbol(match.getSymbol())
                .name(match.getName())
                .exchange(match.getRegion())
                .currency(match.getCurrency())
                .sector("Unknown")
                .industry("Unknown")
                .isPopular(false)
                .stockType(com.capitalot.model.StockType.STOCK)
                .description(match.getName())
                .annualDividend(0.0)
                .risk(5.0)
                .longPercentage(longPct)
                .shortPercentage(shortPct)
                .marketScore(70.0 + random.nextDouble() * 30.0)
                .build();
    }
}
