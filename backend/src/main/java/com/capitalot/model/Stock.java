package com.capitalot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private String name;
    
    private String exchange;
    
    private String currency;
    
    private String sector;
    
    private String industry;
    
    @Column(nullable = false)
    private Boolean isPopular = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockType stockType = StockType.STOCK;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    private Double marketScore;
    
    @Transient
    private Double currentPrice;
    
    @ManyToMany
    @JoinTable(
        name = "stock_tags",
        joinColumns = @JoinColumn(name = "stock_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"stocks", "hibernateLazyInitializer", "handler"})
    private Set<Tag> tags = new HashSet<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @PostLoad
    protected void calculateCurrentPrice() {
        Random random = new Random(symbol.hashCode());
        currentPrice = 50.0 + random.nextDouble() * 450.0;
    }
}
