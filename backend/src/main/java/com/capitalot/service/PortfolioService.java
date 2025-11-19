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

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    
    public List<Portfolio> getUserPortfolios(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return portfolioRepository.findByUserId(user.getId());
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
    public PortfolioStock addStockToPortfolio(Long portfolioId, AddStockRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        Stock stock = stockRepository.findById(request.getStockId())
            .orElseThrow(() -> new RuntimeException("Stock not found"));
        
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
