package com.capitalot.service;

import com.capitalot.dto.AlphaVantageOverviewResponse;
import com.capitalot.dto.AlphaVantageSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AlphaVantageService {

    private final List<String> apiKeys;
    private int currentKeyIndex = 0;

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private final RestTemplate restTemplate;

    public AlphaVantageService(@Value("${app.stock-api.alpha-vantage.api-keys:}") String keysString) {
        this.restTemplate = new RestTemplate();
        if (keysString != null && !keysString.isEmpty()) {
            this.apiKeys = Arrays.asList(keysString.split(","));
        } else {
            this.apiKeys = new ArrayList<>();
        }
    }

    @Cacheable(value = "symbolSearch", key = "#query", unless = "#result == null")
    public Optional<AlphaVantageSearchResponse> search(String query) {
        if (apiKeys.isEmpty()) {
            log.warn("No Alpha Vantage API keys configured.");
            return Optional.empty();
        }

        // Try all available keys in rotation if one fails
        for (int i = 0; i < apiKeys.size(); i++) {
            String apiKey = apiKeys.get(currentKeyIndex);
            try {
                String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                        .queryParam("function", "SYMBOL_SEARCH")
                        .queryParam("keywords", query)
                        .queryParam("apikey", apiKey)
                        .toUriString();

                log.info("Searching Alpha Vantage with key index {} for query: {}", currentKeyIndex, query);
                AlphaVantageSearchResponse response = restTemplate.getForObject(url, AlphaVantageSearchResponse.class);

                if (response != null) {
                    if ((response.getNote() != null && response.getNote().contains("rate limit")) || 
                        (response.getInformation() != null && response.getInformation().contains("rate limit"))) {
                        log.warn("Alpha Vantage rate limit hit for key index {}. Rotating...", currentKeyIndex);
                        rotateKey();
                        continue; // Try next key
                    }
                    
                    if (response.getBestMatches() != null) {
                        return Optional.of(response);
                    }
                }

            } catch (Exception e) {
                log.error("Error searching Alpha Vantage with key index {}: {}", currentKeyIndex, e.getMessage());
                // Rotate to next key for next attempt
                rotateKey();
            }
        }

        return Optional.empty();
    }

    @Cacheable(value = "alphaVantageOverview", key = "#symbol", unless = "#result == null")
    public Optional<AlphaVantageOverviewResponse> getOverview(String symbol) {
        if (apiKeys.isEmpty()) {
            log.warn("No Alpha Vantage API keys configured. Skipping overview for {}", symbol);
            return Optional.empty();
        }

        String apiKey = apiKeys.get(currentKeyIndex);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("function", "OVERVIEW")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            log.info("Fetching Alpha Vantage overview for symbol: {}", symbol);
            AlphaVantageOverviewResponse response = restTemplate.getForObject(url, AlphaVantageOverviewResponse.class);

            if (response != null) {
                if ((response.getNote() != null && response.getNote().contains("rate limit")) ||
                    (response.getInformation() != null && response.getInformation().contains("rate limit"))) {
                    log.warn("Alpha Vantage rate limit hit for OVERVIEW {}. Rotating key.", symbol);
                    rotateKey();
                    return Optional.empty();
                }
                if (response.getSymbol() != null && !response.getSymbol().isBlank()) {
                    return Optional.of(response);
                }
            }

            log.warn("No Alpha Vantage overview found for symbol: {}", symbol);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching Alpha Vantage overview for {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    private void rotateKey() {
        if (!apiKeys.isEmpty()) {
            currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
            log.info("Rotated to Alpha Vantage API key index {}", currentKeyIndex);
        }
    }
}
