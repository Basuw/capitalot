package com.capitalot.service;

import com.capitalot.dto.AddStockRequest;
import com.capitalot.dto.UpdateWatchlistItemRequest;
import com.capitalot.model.*;
import com.capitalot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    
    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    
    public List<Watchlist> getUserWatchlists(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return watchlistRepository.findByUserId(user.getId());
    }
    
    public List<WatchlistItem> getUserWatchlistItems(String email) {
        return watchlistItemRepository.findByUserEmail(email);
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
    public WatchlistItem addItemToWatchlist(String email, AddStockRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Stock stock = stockRepository.findBySymbol(request.getSymbol())
            .orElseGet(() -> {
                Stock newStock = Stock.builder()
                    .symbol(request.getSymbol())
                    .name(request.getName())
                    .stockType(request.getType())
                    .build();
                return stockRepository.save(newStock);
            });
        
        Set<Tag> tags = new HashSet<>();
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder().name(tagName).build();
                        return tagRepository.save(newTag);
                    });
                tags.add(tag);
            }
        }
        
        WatchlistItem item = WatchlistItem.builder()
            .user(user)
            .stock(stock)
            .targetPrice(request.getTargetPrice())
            .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
            .notes(request.getNotes())
            .tags(tags)
            .build();
        
        return watchlistItemRepository.save(item);
    }
    
    @Transactional
    public WatchlistItem updateWatchlistItem(Long itemId, UpdateWatchlistItemRequest request) {
        WatchlistItem item = watchlistItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Watchlist item not found"));
        
        if (request.getTargetPrice() != null) {
            item.setTargetPrice(request.getTargetPrice());
        }
        if (request.getPriority() != null) {
            item.setPriority(request.getPriority());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }
        if (request.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder().name(tagName).build();
                        return tagRepository.save(newTag);
                    });
                tags.add(tag);
            }
            item.setTags(tags);
        }
        
        return watchlistItemRepository.save(item);
    }
    
    @Transactional
    public void removeItemFromWatchlist(Long itemId) {
        watchlistItemRepository.deleteById(itemId);
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
