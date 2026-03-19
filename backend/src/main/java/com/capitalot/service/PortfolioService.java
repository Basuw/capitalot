package com.capitalot.service;

import com.capitalot.dto.AddStockRequest;
import com.capitalot.dto.PerformanceStats;
import com.capitalot.dto.PortfolioPerformanceDto;
import com.capitalot.dto.SellStockRequest;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.model.Portfolio;
import com.capitalot.model.PortfolioStock;
import com.capitalot.model.Stock;
import com.capitalot.model.User;
import com.capitalot.repository.PortfolioRepository;
import com.capitalot.repository.PortfolioStockRepository;
import com.capitalot.repository.StockRepository;
import com.capitalot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;
    private final Random random = new Random();
    
    public List<Portfolio> getUserPortfolios(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());
        
        for (Portfolio portfolio : portfolios) {
            double totalValue = 0.0;
            for (PortfolioStock ps : portfolio.getStocks()) {
                if (ps.getSaleDate() == null) {
                    StockPriceResponse priceResponse = stockPriceService.getStockPrice(ps.getStock().getSymbol());
                    totalValue += priceResponse.getCurrentPrice().doubleValue() * ps.getQuantity().doubleValue();
                }
            }
            portfolio.setTotalValue(totalValue);
        }
        
        return portfolios;
    }
    
    @Transactional(readOnly = true)
    public Portfolio getPortfolioById(Long portfolioId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio");
        }
        
        // Force load stocks
        portfolio.getStocks().size();
        
        // Enrich each stock with current prices
        for (PortfolioStock ps : portfolio.getStocks()) {
            if (ps.getSaleDate() == null) {  // Only for stocks not yet sold
                StockPriceResponse priceResponse = stockPriceService.getStockPrice(ps.getStock().getSymbol());
                
                // Set current price
                ps.setCurrentPrice(priceResponse.getCurrentPrice().doubleValue());
                
                // Calculate current value
                double currentValue = ps.getQuantity().doubleValue() * priceResponse.getCurrentPrice().doubleValue();
                ps.setCurrentValue(currentValue);
                
                // Calculate gain/loss based on PURCHASE price
                double purchaseValue = ps.getQuantity().doubleValue() * ps.getPurchasePrice().doubleValue();
                double gainLoss = currentValue - purchaseValue;
                ps.setGainLoss(gainLoss);
                ps.setGainLossPercentage((gainLoss / purchaseValue) * 100.0);
            }
        }
        
        return portfolio;
    }
    
    @Transactional
    public Portfolio updatePortfolio(Long portfolioId, String email, String name, String description, String icon, String link) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio");
        }
        if (name != null) portfolio.setName(name);
        portfolio.setDescription(description);
        portfolio.setIcon(icon);
        portfolio.setLink(link);
        return portfolioRepository.save(portfolio);
    }

    @Transactional
    public Portfolio createPortfolio(String email, String name, String description, String icon, String link) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Portfolio portfolio = Portfolio.builder()
            .name(name)
            .description(description)
            .icon(icon)
            .link(link)
            .user(user)
            .build();
        
        return portfolioRepository.save(portfolio);
    }
    
    @Transactional
    public PortfolioStock addStockToPortfolio(Long portfolioId, AddStockRequest request, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio");
        }
        
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
        
        PortfolioStock portfolioStock = PortfolioStock.builder()
            .portfolio(portfolio)
            .stock(stock)
            .quantity(request.getQuantity())
            .purchasePrice(request.getPurchasePrice())
            .purchaseDate(request.getPurchaseDate())
            .notes(request.getNotes())
            .build();
        
        return portfolioStockRepository.save(portfolioStock);
    }
    
    public List<PortfolioStock> getPortfolioStocks(Long portfolioId) {
        return portfolioStockRepository.findByPortfolioId(portfolioId);
    }
    
    @Transactional
    public void deletePortfolio(Long portfolioId) {
        portfolioRepository.deleteById(portfolioId);
    }
    
    @Transactional
    public void removeStockFromPortfolio(Long portfolioStockId) {
        portfolioStockRepository.deleteById(portfolioStockId);
    }
    
    @Transactional
    public PortfolioStock sellStock(Long portfolioStockId, SellStockRequest request, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        PortfolioStock portfolioStock = portfolioStockRepository.findById(portfolioStockId)
            .orElseThrow(() -> new RuntimeException("Portfolio stock not found"));
        
        if (!portfolioStock.getPortfolio().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio stock");
        }
        
        portfolioStock.setSalePrice(request.getSalePrice());
        portfolioStock.setSaleDate(request.getSaleDate());
        
        return portfolioStockRepository.save(portfolioStock);
    }
    
    public PerformanceStats getPortfolioPerformance(Long portfolioId, String range, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio");
        }
        
        List<PortfolioStock> stocks = portfolioStockRepository.findByPortfolioId(portfolioId);
        
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        
        // Calculate totals for all stocks not yet sold
        for (PortfolioStock ps : stocks) {
            if (ps.getSaleDate() == null) {
                // Total invested = sum of (purchase price * quantity)
                BigDecimal invested = ps.getPurchasePrice().multiply(ps.getQuantity());
                totalInvested = totalInvested.add(invested);
                
                // Current value = sum of (current price * quantity)
                StockPriceResponse priceResponse = stockPriceService.getStockPrice(ps.getStock().getSymbol());
                BigDecimal current = priceResponse.getCurrentPrice().multiply(ps.getQuantity());
                currentValue = currentValue.add(current);
            }
        }
        
        // Total gain/loss = current value - total invested
        BigDecimal gainLoss = currentValue.subtract(totalInvested);
        BigDecimal gainLossPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
            ? gainLoss.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        List<PortfolioPerformanceDto> performanceHistory = generatePortfolioPerformanceHistory(stocks, range);
        
        return PerformanceStats.builder()
            .totalInvested(totalInvested)
            .currentValue(currentValue)
            .totalGainLoss(gainLoss)
            .totalGainLossPercent(gainLossPercent)
            .numberOfStocks(stocks.size())
            .performanceHistory(performanceHistory)
            .build();
    }
    
    private List<PortfolioPerformanceDto> generatePortfolioPerformanceHistory(List<PortfolioStock> stocks, String range) {
        List<PortfolioPerformanceDto> history = new ArrayList<>();
        
        if (stocks.isEmpty()) {
            return history;
        }
        
        int days = getDaysFromRange(range);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        BigDecimal baseValue = BigDecimal.ZERO;
        for (PortfolioStock ps : stocks) {
            if (ps.getSaleDate() == null) {
                StockPriceResponse priceResponse = stockPriceService.getStockPrice(ps.getStock().getSymbol());
                baseValue = baseValue.add(priceResponse.getCurrentPrice().multiply(ps.getQuantity()));
            }
        }
        
        double baseValueDouble = baseValue.doubleValue();
        
        for (int i = 0; i <= days; i++) {
            LocalDateTime timestamp = startDate.plusDays(i);
            double variation = (random.nextDouble() - 0.5) * 0.02;
            double value = baseValueDouble * (1 + variation);
            
            history.add(PortfolioPerformanceDto.builder()
                .timestamp(timestamp)
                .totalValue(value)
                .build());
        }
        
        return history;
    }
    
    private int getDaysFromRange(String range) {
        return switch (range) {
            case "1D" -> 1;
            case "1W" -> 7;
            case "1M" -> 30;
            case "3M" -> 90;
            case "6M" -> 180;
            case "1Y" -> 365;
            default -> 1825; // 5 years for ALL
        };
    }
}
