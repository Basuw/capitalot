package com.capitalot.service;

import com.capitalot.dto.UserPreferencesDto;
import com.capitalot.model.User;
import com.capitalot.model.UserPreferences;
import com.capitalot.repository.UserPreferencesRepository;
import com.capitalot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferencesService {
    
    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public UserPreferencesDto getUserPreferences(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferences preferences = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user.getId()));
        
        return convertToDto(preferences);
    }
    
    @Transactional
    public UserPreferencesDto updateUserPreferences(String email, UserPreferencesDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferences preferences = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user.getId()));
        
        // Update preferences
        if (dto.getShowAnnualizedReturn() != null) {
            preferences.setShowAnnualizedReturn(dto.getShowAnnualizedReturn());
        }
        if (dto.getShowBenchmarkComparison() != null) {
            preferences.setShowBenchmarkComparison(dto.getShowBenchmarkComparison());
        }
        if (dto.getShowBestWorstDay() != null) {
            preferences.setShowBestWorstDay(dto.getShowBestWorstDay());
        }
        if (dto.getShowStartPriceLine() != null) {
            preferences.setShowStartPriceLine(dto.getShowStartPriceLine());
        }
        if (dto.getBenchmarkSymbol() != null && !dto.getBenchmarkSymbol().isEmpty()) {
            preferences.setBenchmarkSymbol(dto.getBenchmarkSymbol());
        }
        if (dto.getShowPerformanceBadge() != null) {
            preferences.setShowPerformanceBadge(dto.getShowPerformanceBadge());
        }
        if (dto.getShowDetailedMetrics() != null) {
            preferences.setShowDetailedMetrics(dto.getShowDetailedMetrics());
        }
        
        preferences = preferencesRepository.save(preferences);
        log.info("Updated preferences for user {}", email);
        
        return convertToDto(preferences);
    }
    
    private UserPreferences createDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .showAnnualizedReturn(false)
                .showBenchmarkComparison(false)
                .showBestWorstDay(false)
                .showStartPriceLine(false)
                .benchmarkSymbol("SPY")
                .showPerformanceBadge(true)
                .showDetailedMetrics(true)
                .build();
        
        return preferencesRepository.save(preferences);
    }
    
    private UserPreferencesDto convertToDto(UserPreferences preferences) {
        return UserPreferencesDto.builder()
                .id(preferences.getId())
                .showAnnualizedReturn(preferences.getShowAnnualizedReturn())
                .showBenchmarkComparison(preferences.getShowBenchmarkComparison())
                .showBestWorstDay(preferences.getShowBestWorstDay())
                .showStartPriceLine(preferences.getShowStartPriceLine())
                .benchmarkSymbol(preferences.getBenchmarkSymbol())
                .showPerformanceBadge(preferences.getShowPerformanceBadge())
                .showDetailedMetrics(preferences.getShowDetailedMetrics())
                .build();
    }
}
