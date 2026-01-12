package com.capitalot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_stock_id", nullable = false)
    @JsonIgnoreProperties({"purchases", "portfolio", "stock"})
    private PortfolioStock portfolioStock;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal purchasePrice;
    
    @Column(nullable = false)
    private LocalDateTime purchaseDate;
    
    private String notes;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
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
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
    }
}
