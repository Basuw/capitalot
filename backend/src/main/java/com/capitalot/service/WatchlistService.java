package com.capitalot.service;

import com.capitalot.model.Stock;
import com.capitalot.model.User;
import com.capitalot.model.Watchlist;
import com.capitalot.repository.StockRepository;
import com.capitalot.repository.UserRepository;
import com.capitalot.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    
    private final WatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    
    public List<Watchlist> getUserWatchlists(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return watchlistRepository.findByUserId(user.getId());
    }
    
    @Transactional
    public Watchlist createWatchlist(String email, String name, String description, String icon, String link) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Watchlist watchlist = Watchlist.builder()
            .name(name)
            .description(description)
            .icon(icon)
            .link(link)
            .user(user)
            .build();
        
        return watchlistRepository.save(watchlist);
    }
    
    @Transactional
    public Watchlist addStockToWatchlist(Long watchlistId, Long stockId) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
            .orElseThrow(() -> new RuntimeException("Watchlist not found"));
        
        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        watchlist.getStocks().add(stock);
        return watchlistRepository.save(watchlist);
    }
    
    @Transactional
    public Watchlist removeStockFromWatchlist(Long watchlistId, Long stockId) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
            .orElseThrow(() -> new RuntimeException("Watchlist not found"));
        
        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        watchlist.getStocks().remove(stock);
        return watchlistRepository.save(watchlist);
    }
    
    @Transactional
    public void deleteWatchlist(Long watchlistId) {
        watchlistRepository.deleteById(watchlistId);
    }
}
