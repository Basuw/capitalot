package com.capitalot.service;

import com.capitalot.dto.AddStockRequest;
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
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    
    public List<Portfolio> getUserPortfolios(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());
        
        for (Portfolio portfolio : portfolios) {
            double totalValue = 0.0;
            for (PortfolioStock ps : portfolio.getStocks()) {
                if (ps.getCurrentValue() != null) {
                    totalValue += ps.getCurrentValue();
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
        portfolio.getStocks().size();
        return portfolio;
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
}
