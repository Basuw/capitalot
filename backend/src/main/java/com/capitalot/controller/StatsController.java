package com.capitalot.controller;

import com.capitalot.dto.PerformanceStats;
import com.capitalot.dto.PortfolioPerformanceDto;
import com.capitalot.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    
    private final StatsService statsService;
    
    @GetMapping("/daily")
    public ResponseEntity<PerformanceStats> getDailyStats(Authentication authentication) {
        return ResponseEntity.ok(statsService.getDailyStats(authentication.getName()));
    }
    
    @GetMapping("/monthly")
    public ResponseEntity<PerformanceStats> getMonthlyStats(Authentication authentication) {
        return ResponseEntity.ok(statsService.getMonthlyStats(authentication.getName()));
    }
    
    @GetMapping("/yearly")
    public ResponseEntity<PerformanceStats> getYearlyStats(Authentication authentication) {
        return ResponseEntity.ok(statsService.getYearlyStats(authentication.getName()));
    }
    
    @GetMapping("/all-time")
    public ResponseEntity<PerformanceStats> getAllTimeStats(Authentication authentication) {
        return ResponseEntity.ok(statsService.getAllTimeStats(authentication.getName()));
    }
    
    @GetMapping("/performance-history")
    public ResponseEntity<List<PortfolioPerformanceDto>> getPerformanceHistory(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "1M") String period) {
        return ResponseEntity.ok(statsService.getPerformanceHistory(authentication.getName(), period));
    }
}
