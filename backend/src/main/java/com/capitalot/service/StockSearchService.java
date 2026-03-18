package com.capitalot.service;

import com.capitalot.dto.FinnhubSearchResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceSearchResponse;
import com.capitalot.model.Stock;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockSearchService {
    
    private final StockRepository stockRepository;
    private final StockEnrichmentService stockEnrichmentService;
    private final YahooFinanceService yahooFinanceService;
    private final FinnhubService finnhubService;
    private final Random random = new Random();
    
    public List<Stock> searchStocks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getPopularStocks();
        }

        query = query.trim();
        log.info("Starting multi-source search for: {}", query);

        // Map pour éviter les doublons (Clé = Symbol)
        Map<String, Stock> combinedResults = new LinkedHashMap<>();

        // 1. Rechercher en base de données locale
        List<Stock> localStocks = stockRepository.searchStocks(query);
        localStocks.forEach(s -> combinedResults.put(s.getSymbol().toUpperCase(), s));

        // 2. Finnhub est la source principale de recherche (toujours appelé)
        try {
            var finnhubOpt = finnhubService.search(query);
            if (finnhubOpt.isPresent() && finnhubOpt.get().getResult() != null) {
                for (var result : finnhubOpt.get().getResult()) {
                    String symbol = result.getSymbol().toUpperCase();
                    if (!combinedResults.containsKey(symbol)) {
                        Stock stock = convertFinnhubResultToStock(result);
                        combinedResults.put(symbol, stock);
                    }
                }
                log.info("Finnhub returned {} results for query: {}", finnhubOpt.get().getResult().size(), query);
            }
        } catch (Exception e) {
            log.warn("Finnhub search failed: {}", e.getMessage());
        }

        // 3. Yahoo Finance search pour enrichir les résultats (secteur, industrie, nom complet, bourse)
        try {
            var yahooOpt = yahooFinanceService.search(query);
            if (yahooOpt.isPresent() && yahooOpt.get().getQuotes() != null) {
                for (var quote : yahooOpt.get().getQuotes()) {
                    String symbol = quote.getSymbol().toUpperCase();
                    if (combinedResults.containsKey(symbol)) {
                        // Enrichir le stock Finnhub existant avec les infos Yahoo Finance
                        Stock existing = combinedResults.get(symbol);
                        if (quote.getLongname() != null && !quote.getLongname().isBlank()) {
                            existing.setName(quote.getLongname());
                            existing.setDescription(quote.getLongname());
                        } else if (quote.getShortname() != null && !quote.getShortname().isBlank() && "Unknown".equals(existing.getName())) {
                            existing.setName(quote.getShortname());
                            existing.setDescription(quote.getShortname());
                        }
                        if (quote.getSector() != null && !quote.getSector().isBlank()) {
                            existing.setSector(quote.getSector());
                        }
                        if (quote.getIndustry() != null && !quote.getIndustry().isBlank()) {
                            existing.setIndustry(quote.getIndustry());
                        }
                        if (quote.getExchange() != null && !quote.getExchange().isBlank()) {
                            existing.setExchange(quote.getExchange());
                        }
                    } else {
                        // Ajouter les symboles supplémentaires trouvés par Yahoo Finance
                        Stock stock = convertYahooQuoteToStock(quote);
                        combinedResults.put(symbol, stock);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Yahoo Finance search failed: {}", e.getMessage());
        }

        // 4. Sauvegarder les nouvelles actions en base pour plus tard
        List<Stock> allStocks = new ArrayList<>(combinedResults.values());
        saveNewStocksToDb(allStocks);

        // 5. Enrichir avec les prix temps réel via Yahoo Finance chart API
        return stockEnrichmentService.enrichStocksWithRealPrices(allStocks);
    }

    private void saveNewStocksToDb(List<Stock> stocks) {
        for (Stock stock : stocks) {
            if (stock.getId() == null && !stockRepository.existsBySymbol(stock.getSymbol())) {
                try {
                    stockRepository.save(stock);
                } catch (Exception e) {
                    log.error("Could not save stock {}: {}", stock.getSymbol(), e.getMessage());
                }
            }
        }
    }

    private Stock convertFinnhubResultToStock(FinnhubSearchResponse.Result result) {
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
                .longPercentage(50.0 + random.nextDouble() * 30)
                .shortPercentage(20.0 + random.nextDouble() * 10)
                .marketScore(7.0 + random.nextDouble() * 2)
                .build();
    }

    private Stock convertYahooQuoteToStock(YahooFinanceSearchResponse.Quote quote) {
        String name = quote.getLongname() != null ? quote.getLongname() : 
                     (quote.getShortname() != null ? quote.getShortname() : quote.getSymbol());
        
        return Stock.builder()
                .symbol(quote.getSymbol())
                .name(name)
                .exchange(quote.getExchange() != null ? quote.getExchange() : "Unknown")
                .currency("USD")
                .sector(quote.getSector() != null ? quote.getSector() : "Unknown")
                .industry(quote.getIndustry() != null ? quote.getIndustry() : "Unknown")
                .isPopular(false)
                .stockType(com.capitalot.model.StockType.STOCK)
                .description(name)
                .annualDividend(0.0)
                .risk(5.0)
                .longPercentage(50.0 + random.nextDouble() * 30)
                .shortPercentage(20.0 + random.nextDouble() * 10)
                .marketScore(7.0 + random.nextDouble() * 2)
                .build();
    }
    
    public List<Stock> getPopularStocks() {
        List<Stock> stocks = stockRepository.findPopularStocks();
        return stockEnrichmentService.enrichStocksWithRealPrices(stocks);
    }
    
    public Stock getStockBySymbol(String symbol) {
        log.info("Fetching details for symbol: {}", symbol);
        
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
            .orElseGet(() -> {
                log.info("Stock {} not found in database, searching online...", symbol);
                // On essaie Yahoo d'abord pour les détails riches
                var yahooResults = yahooFinanceService.search(symbol);
                if (yahooResults.isPresent() && yahooResults.get().getQuotes() != null) {
                    return yahooResults.get().getQuotes().stream()
                            .filter(q -> q.getSymbol().equalsIgnoreCase(symbol))
                            .findFirst()
                            .map(this::convertYahooQuoteToStock)
                            .map(stockRepository::save)
                            .orElseGet(() -> {
                                // Fallback Finnhub
                                var finnhubResults = finnhubService.search(symbol);
                                return finnhubResults.flatMap(res -> res.getResult().stream()
                                        .filter(r -> r.getSymbol().equalsIgnoreCase(symbol))
                                        .findFirst()
                                        .map(this::convertFinnhubResultToStock)
                                        .map(stockRepository::save))
                                        .orElseThrow(() -> new RuntimeException("Stock non trouvé: " + symbol));
                            });
                }
                throw new RuntimeException("Stock non trouvé: " + symbol);
            });
        
        stockEnrichmentService.enrichStockWithRealPrice(stock);
        stockEnrichmentService.enrichStockFundamentals(stock);
        try {
            stock.setNews(yahooFinanceService.getNewsForSymbol(symbol));
        } catch (Exception e) {
            stock.setNews(Collections.emptyList());
        }
        
        return stock;
    }
    
    public void initializePopularStocks() {
        if (stockRepository.count() == 0) {
            List<Stock> popularStocks = Arrays.asList(
                createStock("AAPL", "Apple Inc.", "NASDAQ", "Technology", "Consumer Electronics", true, "Apple designs, manufactures, and markets smartphones...", 0.96, 4.5),
                createStock("MSFT", "Microsoft Corporation", "NASDAQ", "Technology", "Software", true, "Microsoft develops, licenses, and supports software...", 0.75, 3.2),
                createStock("NFLX", "Netflix Inc.", "NASDAQ", "Communication Services", "Entertainment", true, "Netflix is an American subscription streaming service...", 0.0, 6.5)
            );
            stockRepository.saveAll(popularStocks);
        }
    }
    
    private Stock createStock(String symbol, String name, String exchange, String sector, String industry, boolean isPopular, String description, double annualDividend, double risk) {
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
            .longPercentage(60.0)
            .shortPercentage(40.0)
            .marketScore(8.5)
            .build();
    }
}
