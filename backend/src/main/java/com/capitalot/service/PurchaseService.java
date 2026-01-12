package com.capitalot.service;

import com.capitalot.dto.AddStockRequest;
import com.capitalot.dto.PurchaseDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.Portfolio;
import com.capitalot.model.PortfolioStock;
import com.capitalot.model.Purchase;
import com.capitalot.model.Stock;
import com.capitalot.repository.PortfolioRepository;
import com.capitalot.repository.PortfolioStockRepository;
import com.capitalot.repository.PurchaseRepository;
import com.capitalot.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {
    
    private final PurchaseRepository purchaseRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final StockPriceService stockPriceService;
    
    @Transactional
    public Purchase addPurchase(Long portfolioId, AddStockRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        Stock stock;
        if (request.getStockId() != null) {
            stock = stockRepository.findById(request.getStockId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));
        } else if (request.getSymbol() != null) {
            stock = stockRepository.findBySymbol(request.getSymbol())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));
        } else {
            throw new RuntimeException("Stock ID or symbol is required");
        }
        
        // Find or create PortfolioStock
        PortfolioStock portfolioStock = portfolioStockRepository
                .findByPortfolioIdAndStockId(portfolioId, stock.getId())
                .orElseGet(() -> {
                    PortfolioStock ps = PortfolioStock.builder()
                            .portfolio(portfolio)
                            .stock(stock)
                            .quantity(BigDecimal.ZERO)
                            .purchasePrice(BigDecimal.ZERO)
                            .purchaseDate(LocalDateTime.now())
                            .build();
                    return portfolioStockRepository.save(ps);
                });
        
        // Determine purchase price
        BigDecimal purchasePrice = request.getPurchasePrice();
        if (request.getUseMarketPrice() != null && request.getUseMarketPrice()) {
            // Get historical price for the purchase date
            LocalDateTime purchaseDate = request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDateTime.now();
            purchasePrice = stockPriceService.getHistoricalPrice(stock.getSymbol(), purchaseDate);
            log.info("Using historical price: {} for {} on {}", purchasePrice, stock.getSymbol(), purchaseDate);
        } else if (purchasePrice == null) {
            // Fallback: use current price
            StockPriceResponse priceResponse = stockPriceService.getStockPrice(stock.getSymbol());
            purchasePrice = priceResponse.getCurrentPrice();
            log.warn("No purchase price provided, using current price: {}", purchasePrice);
        }
        
        // Create purchase
        Purchase purchase = Purchase.builder()
                .portfolioStock(portfolioStock)
                .quantity(request.getQuantity())
                .purchasePrice(purchasePrice)
                .purchaseDate(request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDateTime.now())
                .notes(request.getNotes())
                .build();
        
        purchase = purchaseRepository.save(purchase);
        
        // Update PortfolioStock aggregates
        updatePortfolioStockAggregates(portfolioStock);
        
        return purchase;
    }
    
    @Transactional(readOnly = true)
    public List<PurchaseDto> getPurchaseHistory(Long portfolioStockId) {
        List<Purchase> purchases = purchaseRepository.findByPortfolioStockId(portfolioStockId);
        
        // Get current price for calculations
        PortfolioStock portfolioStock = portfolioStockRepository.findById(portfolioStockId)
                .orElseThrow(() -> new RuntimeException("Portfolio stock not found"));
        
        StockPriceResponse priceResponse = stockPriceService.getStockPrice(portfolioStock.getStock().getSymbol());
        double currentPrice = priceResponse.getCurrentPrice().doubleValue();
        
        return purchases.stream()
                .map(purchase -> {
                    double currentValue = purchase.getQuantity().doubleValue() * currentPrice;
                    double purchaseValue = purchase.getQuantity().doubleValue() * purchase.getPurchasePrice().doubleValue();
                    double gainLoss = currentValue - purchaseValue;
                    double gainLossPercentage = (gainLoss / purchaseValue) * 100.0;
                    
                    return PurchaseDto.builder()
                            .id(purchase.getId())
                            .quantity(purchase.getQuantity())
                            .purchasePrice(purchase.getPurchasePrice())
                            .purchaseDate(purchase.getPurchaseDate())
                            .notes(purchase.getNotes())
                            .currentPrice(currentPrice)
                            .currentValue(currentValue)
                            .gainLoss(gainLoss)
                            .gainLossPercentage(gainLossPercentage)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deletePurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
        
        PortfolioStock portfolioStock = purchase.getPortfolioStock();
        purchaseRepository.delete(purchase);
        
        // Update aggregates or delete PortfolioStock if no more purchases
        List<Purchase> remainingPurchases = purchaseRepository.findByPortfolioStockId(portfolioStock.getId());
        if (remainingPurchases.isEmpty()) {
            portfolioStockRepository.delete(portfolioStock);
        } else {
            updatePortfolioStockAggregates(portfolioStock);
        }
    }
    
    private void updatePortfolioStockAggregates(PortfolioStock portfolioStock) {
        List<Purchase> purchases = purchaseRepository.findByPortfolioStockId(portfolioStock.getId());
        
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (Purchase purchase : purchases) {
            totalQuantity = totalQuantity.add(purchase.getQuantity());
            totalCost = totalCost.add(purchase.getPurchasePrice().multiply(purchase.getQuantity()));
        }
        
        // Calculate weighted average purchase price
        BigDecimal avgPurchasePrice = totalQuantity.compareTo(BigDecimal.ZERO) > 0
                ? totalCost.divide(totalQuantity, 4, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        
        portfolioStock.setQuantity(totalQuantity);
        portfolioStock.setPurchasePrice(avgPurchasePrice);
        
        portfolioStockRepository.save(portfolioStock);
    }
}
