package com.capitalot.dto;

import com.capitalot.model.Priority;
import com.capitalot.model.StockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddStockRequest {
    private Long stockId;
    private String symbol;
    private String name;
    private StockType type;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private LocalDateTime purchaseDate;
    private String notes;
    private Double targetPrice;
    private Priority priority;
    private Set<String> tags;
}
