package com.capitalot.controller;

import com.capitalot.dto.PerformanceStats;
import com.capitalot.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
