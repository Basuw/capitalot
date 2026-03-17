package com.capitalot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnoreProperties({"portfolios", "watchlists"})
    private User user;
    
    // Chart Display Preferences
    @Column(nullable = false)
    @Builder.Default
    private Boolean showAnnualizedReturn = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean showBenchmarkComparison = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean showBestWorstDay = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean showStartPriceLine = false;
    
    // Benchmark settings
    @Column(nullable = false)
    @Builder.Default
    private String benchmarkSymbol = "SPY"; // S&P 500 ETF
    
    // Currency preference
    @Column(nullable = false)
    @Builder.Default
    private String currency = "USD";
    
    // Display settings
    @Column(nullable = false)
    @Builder.Default
    private Boolean showPerformanceBadge = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean showDetailedMetrics = true;
    
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
}
