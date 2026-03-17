package com.capitalot.service;

import com.capitalot.dto.yahoofinance.YahooFinanceChartResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceQuoteSummaryResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@Slf4j
public class YahooFinanceService {
    
    private static final String BASE_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart";
    private static final String BASE_SEARCH_URL = "https://query2.finance.yahoo.com/v1/finance/search";
    private static final String BASE_QUOTE_SUMMARY_URL = "https://query1.finance.yahoo.com/v10/finance/quoteSummary";
    
    private final RestTemplate restTemplate;
    
    public YahooFinanceService() {
        this.restTemplate = new RestTemplate();
    }
    
    @Cacheable(value = "yahooFinanceChart", key = "#symbol + '-' + #range + '-' + #interval", unless = "#result == null")
    public Optional<YahooFinanceChartResponse> getChartData(String symbol, String range, String interval) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_CHART_URL + "/" + symbol)
                    .queryParam("range", range)
                    .queryParam("interval", interval)
                    .toUriString();
            
            log.info("Fetching chart data for symbol: {} with range: {} and interval: {}", symbol, range, interval);
            YahooFinanceChartResponse response = restTemplate.getForObject(url, YahooFinanceChartResponse.class);
            
            if (isValidChartResponse(response)) {
                return Optional.of(response);
            }
            
            log.warn("No chart data found for symbol: {}", symbol);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error fetching chart data for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    @Cacheable(value = "yahooFinanceChartCustom", key = "#symbol + '-' + #period1 + '-' + #period2 + '-' + #interval", unless = "#result == null")
    public Optional<YahooFinanceChartResponse> getChartDataForPeriod(String symbol, long period1, long period2, String interval) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_CHART_URL + "/" + symbol)
                    .queryParam("period1", period1)
                    .queryParam("period2", period2)
                    .queryParam("interval", interval)
                    .toUriString();
            
            log.info("Fetching chart data for symbol: {} for period {} to {} with interval: {}", symbol, period1, period2, interval);
            YahooFinanceChartResponse response = restTemplate.getForObject(url, YahooFinanceChartResponse.class);
            
            if (isValidChartResponse(response)) {
                return Optional.of(response);
            }
            
            log.warn("No chart data found for symbol: {} in period {} to {}", symbol, period1, period2);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error fetching chart data for symbol: {} in period {} to {}", symbol, period1, period2, e);
            return Optional.empty();
        }
    }

    @Cacheable(value = "yahooFinanceSearch", key = "#query", unless = "#result == null")
    public Optional<YahooFinanceSearchResponse> search(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_SEARCH_URL)
                    .queryParam("q", query)
                    .toUriString();
            
            log.info("[DEBUG] Searching Yahoo Finance for query: {}", query);
            YahooFinanceSearchResponse response = restTemplate.getForObject(url, YahooFinanceSearchResponse.class);
            
            if (response != null && response.getQuotes() != null && !response.getQuotes().isEmpty()) {
                log.info("[DEBUG] Found {} results for query: {}", response.getQuotes().size(), query);
                return Optional.of(response);
            }
            
            log.warn("No search results found for query: {}", query);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error searching Yahoo Finance for query: {}", query, e);
            return Optional.empty();
        }
    }

    @Cacheable(value = "yahooFinanceQuoteSummary", key = "#symbol", unless = "#result == null")
    public Optional<YahooFinanceQuoteSummaryResponse> getQuoteSummary(String symbol) {
        try {
            String modules = "assetProfile,financialData,defaultKeyStatistics,calendarEvents,majorHoldersBreakdown";
            String url = UriComponentsBuilder.fromHttpUrl(BASE_QUOTE_SUMMARY_URL + "/" + symbol)
                    .queryParam("modules", modules)
                    .toUriString();
            
            log.info("Fetching quote summary for symbol: {}", symbol);
            YahooFinanceQuoteSummaryResponse response = restTemplate.getForObject(url, YahooFinanceQuoteSummaryResponse.class);
            
            if (response != null && response.getQuoteSummary() != null && response.getQuoteSummary().getResult() != null &&
                !response.getQuoteSummary().getResult().isEmpty()) {
                return Optional.of(response);
            }
            
            log.warn("No quote summary found for symbol: {}", symbol);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error fetching quote summary for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    private boolean isValidChartResponse(YahooFinanceChartResponse response) {
        return response != null && response.getChart() != null && response.getChart().getResult() != null && 
               !response.getChart().getResult().isEmpty() &&
               response.getChart().getResult().get(0).getTimestamp() != null && 
               !response.getChart().getResult().get(0).getTimestamp().isEmpty() &&
               response.getChart().getResult().get(0).getIndicators() != null && 
               response.getChart().getResult().get(0).getIndicators().getQuote() != null &&
               !response.getChart().getResult().get(0).getIndicators().getQuote().isEmpty();
    }
}
