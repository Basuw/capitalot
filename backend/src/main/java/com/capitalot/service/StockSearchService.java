package com.capitalot.service;

import com.capitalot.dto.yahoofinance.YahooFinanceSearchResponse;
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
    private final YahooFinanceService yahooFinanceService;
    private final AlphaVantageService alphaVantageService;
    private final FinnhubService finnhubService;
    private final Random random = new Random();
    
    public List<Stock> searchStocks(String query) {
        List<Stock> stocks = new java.util.ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            stocks = stockRepository.findPopularStocks();
        } else {
            // 1. Chercher d'abord dans la base de données locale pour la rapidité
            stocks = new java.util.ArrayList<>(stockRepository.searchStocks(query));
            
            // 2. Si on a peu de résultats, on tente d'abord Finnhub (souvent plus fiable pour la recherche simple)
            if (stocks.size() < 5) {
                log.info("Searching Finnhub API for query: {}", query);
                var finnhubResults = finnhubService.search(query);
                
                if (finnhubResults.isPresent() && finnhubResults.get().getResult() != null && !finnhubResults.get().getResult().isEmpty()) {
                    List<Stock> apiStocks = finnhubResults.get().getResult().stream()
                            .map(this::convertFinnhubResultToStock)
                            .collect(Collectors.toList());
                    
                    saveNewStocks(apiStocks, stocks);
                    log.info("Found {} stocks from Finnhub", apiStocks.size());
                } else {
                    // 3. FALLBACK 1: Alpha Vantage
                    log.info("Finnhub failed or no results, falling back to Alpha Vantage for query: {}", query);
                    var avResults = alphaVantageService.search(query);
                    
                    if (avResults.isPresent() && avResults.get().getBestMatches() != null && !avResults.get().getBestMatches().isEmpty()) {
                        List<Stock> apiStocks = avResults.get().getBestMatches().stream()
                                .map(this::convertAlphaVantageMatchToStock)
                                .collect(Collectors.toList());
                        
                        saveNewStocks(apiStocks, stocks);
                        log.info("Found {} stocks from Alpha Vantage fallback", apiStocks.size());
                    } else {
                        // 4. FALLBACK 2: Yahoo Finance
                        log.info("Alpha Vantage failed or no results, falling back to Yahoo Finance for query: {}", query);
                        var yahooResults = yahooFinanceService.search(query);
                        
                        if (yahooResults.isPresent() && yahooResults.get().getQuotes() != null) {
                            List<Stock> apiStocks = yahooResults.get().getQuotes().stream()
                                    .filter(quote -> "EQUITY".equalsIgnoreCase(quote.getQuoteType()) || 
                                                   "ETF".equalsIgnoreCase(quote.getQuoteType()))
                                    .map(this::convertYahooQuoteToStock)
                                    .collect(Collectors.toList());
                            
                            saveNewStocks(apiStocks, stocks);
                            log.info("Found {} stocks from Yahoo Finance fallback", apiStocks.size());
                        }
                    }
                }
            }
        }
        
        // 5. Enrichir TOUS les résultats avec les prix temps réel
        return stockEnrichmentService.enrichStocksWithRealPrices(stocks);
    }

    private Stock convertFinnhubResultToStock(com.capitalot.dto.FinnhubSearchResponse.Result result) {
        double longPct = 50.0 + random.nextDouble() * 40.0;
        double shortPct = 100.0 - longPct;
        
        return Stock.builder()
                .symbol(result.getSymbol())
                .name(result.getDescription())
                .exchange("Unknown")
                .currency("USD")
                .sector("Unknown")
                .industry("Unknown")
                .isPopular(false)
                .stockType(com.capitalot.model.StockType.STOCK)
                .description(result.getDescription())
                .annualDividend(0.0)
                .risk(5.0)
                .longPercentage(longPct)
                .shortPercentage(shortPct)
                .marketScore(70.0 + random.nextDouble() * 30.0)
                .build();
    }

    private void saveNewStocks(List<Stock> apiStocks, List<Stock> currentList) {
        for (Stock apiStock : apiStocks) {
            if (!stockRepository.existsBySymbol(apiStock.getSymbol())) {
                try {
                    stockRepository.save(apiStock);
                    if (currentList.stream().noneMatch(s -> s.getSymbol().equalsIgnoreCase(apiStock.getSymbol()))) {
                        currentList.add(apiStock);
                    }
                } catch (Exception e) {
                    log.error("Error saving stock {}: {}", apiStock.getSymbol(), e.getMessage());
                }
            } else if (currentList.stream().noneMatch(s -> s.getSymbol().equalsIgnoreCase(apiStock.getSymbol()))) {
                stockRepository.findBySymbol(apiStock.getSymbol()).ifPresent(currentList::add);
            }
        }
    }

    private Stock convertYahooQuoteToStock(com.capitalot.dto.yahoofinance.YahooFinanceSearchResponse.Quote quote) {
        double longPct = 50.0 + random.nextDouble() * 40.0;
        double shortPct = 100.0 - longPct;
        
        String name = quote.getLongname() != null ? quote.getLongname() : 
                     (quote.getShortname() != null ? quote.getShortname() : quote.getSymbol());
        
        return Stock.builder()
                .symbol(quote.getSymbol())
                .name(name)
                .exchange(quote.getExchange())
                .currency("USD")
                .sector(quote.getSector() != null ? quote.getSector() : "Unknown")
                .industry(quote.getIndustry() != null ? quote.getIndustry() : "Unknown")
                .isPopular(false)
                .stockType(com.capitalot.model.StockType.STOCK)
                .description(name)
                .annualDividend(0.0)
                .risk(5.0)
                .longPercentage(longPct)
                .shortPercentage(shortPct)
                .marketScore(70.0 + random.nextDouble() * 30.0)
                .build();
    }
    
    public List<Stock> getPopularStocks() {
        List<Stock> stocks = stockRepository.findPopularStocks();
        // Enrichir avec les vrais prix depuis Yahoo Finance
        return stockEnrichmentService.enrichStocksWithRealPrices(stocks);
    }
    
    public Stock getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        
        // Enrichir avec le vrai prix depuis Yahoo Finance
        stockEnrichmentService.enrichStockWithRealPrice(stock);
        
        // Fetch news specifically for detail view
        stock.setNews(yahooFinanceService.getNewsForSymbol(symbol));
        
        return stock;
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
    
    private Stock convertAlphaVantageMatchToStock(com.capitalot.dto.AlphaVantageSearchResponse.Match match) {
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
