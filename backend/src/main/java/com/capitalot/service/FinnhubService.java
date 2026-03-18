package com.capitalot.service;

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

    private static final String BASE_URL = "https://finnhub.io/api/v1/search";
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
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
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
}
