package com.capitalot.controller;

import com.capitalot.dto.PricePointDto;
import com.capitalot.dto.StockPerformanceMetrics;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.Stock;
import com.capitalot.service.StockMetricsService;
import com.capitalot.service.StockPriceService;
import com.capitalot.service.StockSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    
    private final StockSearchService stockSearchService;
    private final StockPriceService stockPriceService;
    private final StockMetricsService stockMetricsService;
    
    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(stockSearchService.searchStocks(query));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<Stock>> getPopularStocks() {
        return ResponseEntity.ok(stockSearchService.getPopularStocks());
    }
    
    // On utilise un préfixe /info/ pour isoler les requêtes sur un symbole spécifique
    // Cela évite que "AAPL/history" ne soit matché par "{symbol}"
    @GetMapping("/info/{symbol:.+}")
    public ResponseEntity<Stock> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockSearchService.getStockBySymbol(symbol));
    }

    @GetMapping("/info/{symbol:.+}/price")
    public ResponseEntity<StockPriceResponse> getStockPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(stockPriceService.getStockPrice(symbol));
    }
    
    @GetMapping("/info/{symbol:.+}/history")
    public ResponseEntity<List<PricePointDto>> getStockHistory(
            @PathVariable String symbol,
            @RequestParam(required = false, defaultValue = "1M") String period) {
        return ResponseEntity.ok(stockPriceService.getStockHistory(symbol, period));
    }
    
    @GetMapping("/info/{symbol:.+}/metrics")
    public ResponseEntity<StockPerformanceMetrics> getStockMetrics(
            @PathVariable String symbol,
            @RequestParam(required = false, defaultValue = "1M") String period,
            @RequestParam(required = false, defaultValue = "SPY") String benchmark) {
        return ResponseEntity.ok(stockMetricsService.calculateMetrics(symbol, period, benchmark));
    }
    
    @GetMapping("/info/{symbol:.+}/historical-price")
    public ResponseEntity<?> getHistoricalPrice(
            @PathVariable String symbol,
            @RequestParam String date) {
        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(date);
            java.math.BigDecimal price = stockPriceService.getHistoricalPrice(symbol, dateTime);
            return ResponseEntity.ok(java.util.Map.of("price", price));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
