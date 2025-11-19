package com.capitalot.dto;

import com.capitalot.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWatchlistItemRequest {
    private Double targetPrice;
    private Priority priority;
    private String notes;
    private Set<String> tags;
}
