package com.capitalot.controller;

import com.capitalot.dto.AddStockRequest;
import com.capitalot.dto.UpdateWatchlistItemRequest;
import com.capitalot.model.Watchlist;
import com.capitalot.model.WatchlistItem;
import com.capitalot.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {
    
    private final WatchlistService watchlistService;
    
    @GetMapping
    public ResponseEntity<List<Watchlist>> getUserWatchlists(Authentication authentication) {
        return ResponseEntity.ok(watchlistService.getUserWatchlists(authentication.getName()));
    }
    
    @GetMapping("/items")
    public ResponseEntity<List<WatchlistItem>> getUserWatchlistItems(Authentication authentication) {
        return ResponseEntity.ok(watchlistService.getUserWatchlistItems(authentication.getName()));
    }
    
    @PostMapping
    public ResponseEntity<Watchlist> createWatchlist(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Watchlist watchlist = watchlistService.createWatchlist(
            authentication.getName(),
            request.get("name"),
            request.get("description"),
            request.get("icon"),
            request.get("link")
        );
        return ResponseEntity.ok(watchlist);
    }
    
    @GetMapping("/{watchlistId}/items")
    public ResponseEntity<List<WatchlistItem>> getWatchlistItems(
            @PathVariable Long watchlistId,
            Authentication authentication) {
        return ResponseEntity.ok(watchlistService.getWatchlistItems(watchlistId, authentication.getName()));
    }
    
    @PostMapping("/{watchlistId}/items")
    public ResponseEntity<WatchlistItem> addItemToWatchlist(
            @PathVariable Long watchlistId,
            @RequestBody AddStockRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(watchlistService.addItemToWatchlist(watchlistId, authentication.getName(), request));
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<WatchlistItem> updateWatchlistItem(
            @PathVariable Long itemId,
            @RequestBody UpdateWatchlistItemRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(watchlistService.updateWatchlistItem(itemId, request));
    }
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromWatchlist(@PathVariable Long itemId) {
        watchlistService.removeItemFromWatchlist(itemId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{watchlistId}/stocks/{stockId}")
    public ResponseEntity<Watchlist> addStock(
            @PathVariable Long watchlistId,
            @PathVariable Long stockId) {
        return ResponseEntity.ok(watchlistService.addStockToWatchlist(watchlistId, stockId));
    }
    
    @DeleteMapping("/{watchlistId}/stocks/{stockId}")
    public ResponseEntity<Watchlist> removeStock(
            @PathVariable Long watchlistId,
            @PathVariable Long stockId) {
        return ResponseEntity.ok(watchlistService.removeStockFromWatchlist(watchlistId, stockId));
    }
    
    @DeleteMapping("/{watchlistId}")
    public ResponseEntity<Void> deleteWatchlist(@PathVariable Long watchlistId) {
        watchlistService.deleteWatchlist(watchlistId);
        return ResponseEntity.noContent().build();
    }
}
