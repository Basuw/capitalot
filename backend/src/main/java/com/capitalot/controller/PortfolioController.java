package com.capitalot.controller;

import com.capitalot.dto.AddStockRequest;
import com.capitalot.model.Portfolio;
import com.capitalot.model.PortfolioStock;
import com.capitalot.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    
    @GetMapping
    public ResponseEntity<List<Portfolio>> getUserPortfolios(Authentication authentication) {
        return ResponseEntity.ok(portfolioService.getUserPortfolios(authentication.getName()));
    }
    
    @GetMapping("/{portfolioId}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable Long portfolioId, Authentication authentication) {
        return ResponseEntity.ok(portfolioService.getPortfolioById(portfolioId, authentication.getName()));
    }
    
    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Portfolio portfolio = portfolioService.createPortfolio(
            authentication.getName(),
            request.get("name"),
            request.get("description"),
            request.get("icon"),
            request.get("link")
        );
        return ResponseEntity.ok(portfolio);
    }
    
    @GetMapping("/{portfolioId}/stocks")
    public ResponseEntity<List<PortfolioStock>> getPortfolioStocks(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(portfolioService.getPortfolioStocks(portfolioId));
    }
    
    @PostMapping("/{portfolioId}/stocks")
    public ResponseEntity<PortfolioStock> addStock(
            @PathVariable Long portfolioId,
            @RequestBody AddStockRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(portfolioService.addStockToPortfolio(portfolioId, request, authentication.getName()));
    }
    
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/stocks/{portfolioStockId}")
    public ResponseEntity<Void> removeStock(@PathVariable Long portfolioStockId) {
        portfolioService.removeStockFromPortfolio(portfolioStockId);
        return ResponseEntity.noContent().build();
    }
}
