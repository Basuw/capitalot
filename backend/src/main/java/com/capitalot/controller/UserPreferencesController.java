package com.capitalot.controller;

import com.capitalot.dto.UserPreferencesDto;
import com.capitalot.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/preferences")
@RequiredArgsConstructor
public class UserPreferencesController {
    
    private final UserPreferencesService preferencesService;
    
    @GetMapping
    public ResponseEntity<UserPreferencesDto> getUserPreferences(Authentication authentication) {
        return ResponseEntity.ok(preferencesService.getUserPreferences(authentication.getName()));
    }
    
    @PutMapping
    public ResponseEntity<UserPreferencesDto> updateUserPreferences(
            Authentication authentication,
            @RequestBody UserPreferencesDto preferencesDto) {
        return ResponseEntity.ok(preferencesService.updateUserPreferences(
                authentication.getName(), preferencesDto));
    }
}
