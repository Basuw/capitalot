package com.capitalot.controller;

import com.capitalot.dto.AddStockRequest;
import com.capitalot.dto.PurchaseDto;
import com.capitalot.model.Purchase;
import com.capitalot.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    
    private final PurchaseService purchaseService;
    
    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Purchase> addPurchase(
            @PathVariable Long portfolioId,
            @RequestBody AddStockRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(purchaseService.addPurchase(portfolioId, request));
    }
    
    @GetMapping("/portfolio-stock/{portfolioStockId}")
    public ResponseEntity<List<PurchaseDto>> getPurchaseHistory(@PathVariable Long portfolioStockId) {
        return ResponseEntity.ok(purchaseService.getPurchaseHistory(portfolioStockId));
    }
    
    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long purchaseId) {
        purchaseService.deletePurchase(purchaseId);
        return ResponseEntity.noContent().build();
    }
}
