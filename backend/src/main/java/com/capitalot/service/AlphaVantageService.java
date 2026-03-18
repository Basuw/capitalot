package com.capitalot.service;

import com.capitalot.dto.AlphaVantageSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@Slf4j
public class AlphaVantageService {

    @Value("${app.stock-api.alpha-vantage.api-key}")
    private String apiKey;

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private final RestTemplate restTemplate;

    public AlphaVantageService() {
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(value = "symbolSearch", key = "#query", unless = "#result == null")
    public Optional<AlphaVantageSearchResponse> search(String query) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Alpha Vantage API key is missing. Skipping search.");
            return Optional.empty();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("function", "SYMBOL_SEARCH")
                    .queryParam("keywords", query)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            log.info("Searching Alpha Vantage for query: {}", query);
            AlphaVantageSearchResponse response = restTemplate.getForObject(url, AlphaVantageSearchResponse.class);

            if (response != null && response.getBestMatches() != null && !response.getBestMatches().isEmpty()) {
                log.info("Found {} results for query: {} in Alpha Vantage", response.getBestMatches().size(), query);
                return Optional.of(response);
            }

            log.warn("No results found for query: {} in Alpha Vantage", query);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error searching Alpha Vantage for query: {}", query, e);
            return Optional.empty();
        }
    }
}
