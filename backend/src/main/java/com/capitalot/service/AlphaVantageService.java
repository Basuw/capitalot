package com.capitalot.service;

import com.capitalot.dto.alphavantage.AlphaVantageQuoteResponse;
import com.capitalot.dto.alphavantage.AlphaVantageSearchResponse;
import com.capitalot.dto.alphavantage.AlphaVantageTimeSeriesResponse;
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
    
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    
    @Value("${app.stock-api.alpha-vantage.api-key:demo}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    public AlphaVantageService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Get real-time quote for a symbol
     * Cached for 1 minute to avoid hitting API rate limits
     */
    @Cacheable(value = "stockQuotes", key = "#symbol", unless = "#result == null")
    public Optional<AlphaVantageQuoteResponse> getQuote(String symbol) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("function", "GLOBAL_QUOTE")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", apiKey)
                    .toUriString();
            
            log.info("Fetching quote for symbol: {} from Alpha Vantage", symbol);
            AlphaVantageQuoteResponse response = restTemplate.getForObject(url, AlphaVantageQuoteResponse.class);
            
            if (response != null && response.getGlobalQuote() != null && response.getGlobalQuote().getSymbol() != null) {
                return Optional.of(response);
            }
            
            log.warn("No quote data found for symbol: {}", symbol);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error fetching quote for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get daily time series data
     * Cached for 1 hour since historical data doesn't change frequently
     */
    @Cacheable(value = "dailyTimeSeries", key = "#symbol", unless = "#result == null")
    public Optional<AlphaVantageTimeSeriesResponse> getDailyTimeSeries(String symbol) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("function", "TIME_SERIES_DAILY")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", apiKey)
                    .queryParam("outputsize", "compact") // Last 100 data points
                    .toUriString();
            
            log.info("Fetching daily time series for symbol: {} from Alpha Vantage", symbol);
            AlphaVantageTimeSeriesResponse response = restTemplate.getForObject(url, AlphaVantageTimeSeriesResponse.class);
            
            if (response != null && response.getTimeSeriesDaily() != null && !response.getTimeSeriesDaily().isEmpty()) {
                return Optional.of(response);
            }
            
            log.warn("No time series data found for symbol: {}", symbol);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error fetching time series for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get intraday time series data (5min interval)
     */
    @Cacheable(value = "intradayTimeSeries", key = "#symbol", unless = "#result == null")
    public Optional<AlphaVantageTimeSeriesResponse> getIntradayTimeSeries(String symbol) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("function", "TIME_SERIES_INTRADAY")
                    .queryParam("symbol", symbol)
                    .queryParam("interval", "5min")
                    .queryParam("apikey", apiKey)
                    .queryParam("outputsize", "compact")
                    .toUriString();
            
            log.info("Fetching intraday time series for symbol: {} from Alpha Vantage", symbol);
            AlphaVantageTimeSeriesResponse response = restTemplate.getForObject(url, AlphaVantageTimeSeriesResponse.class);
            
            if (response != null && response.getTimeSeriesIntraday() != null && !response.getTimeSeriesIntraday().isEmpty()) {
                return Optional.of(response);
            }
            
            log.warn("No intraday data found for symbol: {}", symbol);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error fetching intraday data for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }
    
    /**
     * Search for stock symbols
     * Cached for 24 hours since symbol information doesn't change frequently
     */
    @Cacheable(value = "symbolSearch", key = "#keywords", unless = "#result == null")
    public Optional<AlphaVantageSearchResponse> searchSymbol(String keywords) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("function", "SYMBOL_SEARCH")
                    .queryParam("keywords", keywords)
                    .queryParam("apikey", apiKey)
                    .toUriString();
            
            log.info("Searching for symbols with keywords: {} from Alpha Vantage", keywords);
            AlphaVantageSearchResponse response = restTemplate.getForObject(url, AlphaVantageSearchResponse.class);
            
            if (response != null && response.getBestMatches() != null && !response.getBestMatches().isEmpty()) {
                return Optional.of(response);
            }
            
            log.warn("No symbols found for keywords: {}", keywords);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error searching symbols for keywords: {}", keywords, e);
            return Optional.empty();
        }
    }
}
