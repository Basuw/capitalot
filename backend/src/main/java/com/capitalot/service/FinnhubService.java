package com.capitalot.service;

import com.capitalot.dto.FinnhubMetricResponse;
import com.capitalot.dto.FinnhubProfileResponse;
import com.capitalot.dto.FinnhubSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@Slf4j
public class FinnhubService {

    @Value("${app.stock-api.finnhub.api-key:}")
    private String apiKey;

    private static final String SEARCH_URL = "https://finnhub.io/api/v1/search";
    private static final String PROFILE_URL = "https://finnhub.io/api/v1/stock/profile2";
    private static final String METRIC_URL = "https://finnhub.io/api/v1/stock/metric";
    private final RestTemplate restTemplate;

    public FinnhubService() {
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(value = "finnhubSearch", key = "#query", unless = "#result == null")
    public Optional<FinnhubSearchResponse> search(String query) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Finnhub API key is missing. Skipping search.");
            return Optional.empty();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                    .queryParam("q", query)
                    .queryParam("token", apiKey)
                    .toUriString();

            log.info("Searching Finnhub for query: {}", query);
            FinnhubSearchResponse response = restTemplate.getForObject(url, FinnhubSearchResponse.class);

            if (response != null && response.getResult() != null && !response.getResult().isEmpty()) {
                log.info("Found {} results for query: {} in Finnhub", response.getResult().size(), query);
                return Optional.of(response);
            }

            log.warn("No results found for query: {} in Finnhub", query);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error searching Finnhub for query: {}", query, e);
            return Optional.empty();
        }
    }

    @Cacheable(value = "finnhubProfile", key = "#symbol", unless = "#result == null")
    public Optional<FinnhubProfileResponse> getProfile(String symbol) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(PROFILE_URL)
                    .queryParam("symbol", symbol)
                    .queryParam("token", apiKey)
                    .toUriString();

            log.info("Fetching Finnhub profile for symbol: {}", symbol);
            FinnhubProfileResponse response = restTemplate.getForObject(url, FinnhubProfileResponse.class);

            if (response != null && response.getTicker() != null && !response.getTicker().isBlank()) {
                return Optional.of(response);
            }

            log.warn("No Finnhub profile found for symbol: {}", symbol);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching Finnhub profile for {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    @Cacheable(value = "finnhubMetric", key = "#symbol", unless = "#result == null")
    public Optional<FinnhubMetricResponse> getMetrics(String symbol) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(METRIC_URL)
                    .queryParam("symbol", symbol)
                    .queryParam("metric", "all")
                    .queryParam("token", apiKey)
                    .toUriString();

            log.info("Fetching Finnhub metrics for symbol: {}", symbol);
            FinnhubMetricResponse response = restTemplate.getForObject(url, FinnhubMetricResponse.class);

            if (response != null && response.getMetric() != null && !response.getMetric().isEmpty()) {
                return Optional.of(response);
            }

            log.warn("No Finnhub metrics found for symbol: {}", symbol);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching Finnhub metrics for {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }
}
