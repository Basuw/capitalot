package com.capitalot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PortfolioStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @JsonIgnoreProperties({"stocks", "user"})
    private Portfolio portfolio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal purchasePrice;
    
    @Column(nullable = false)
    private LocalDateTime purchaseDate;
    
    private String notes;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Transient
    private Double currentPrice;
    
    @Transient
    private Double currentValue;
    
    @Transient
    private Double gainLoss;
    
    @Transient
    private Double gainLossPercentage;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @PostLoad
    protected void calculateValues() {
        if (stock != null) {
            this.currentPrice = stock.getCurrentPrice();
            if (this.currentPrice != null && quantity != null) {
                this.currentValue = quantity.doubleValue() * this.currentPrice;
                if (purchasePrice != null) {
                    double purchaseValue = quantity.doubleValue() * purchasePrice.doubleValue();
                    this.gainLoss = this.currentValue - purchaseValue;
                    this.gainLossPercentage = (this.gainLoss / purchaseValue) * 100.0;
                }
            }
        }
    }
}
