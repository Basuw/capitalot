package com.capitalot.controller;

import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.Stock;
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
    
    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(stockSearchService.searchStocks(query));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<Stock>> getPopularStocks() {
        return ResponseEntity.ok(stockSearchService.getPopularStocks());
    }
    
    @GetMapping("/{symbol}/price")
    public ResponseEntity<StockPriceResponse> getStockPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(stockPriceService.getStockPrice(symbol));
    }
}
