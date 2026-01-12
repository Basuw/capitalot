package com.capitalot.dto.alphavantage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AlphaVantageTimeSeriesResponse {
    @JsonProperty("Meta Data")
    private MetaData metaData;
    
    @JsonProperty("Time Series (Daily)")
    private Map<String, DailyData> timeSeriesDaily;
    
    @JsonProperty("Time Series (Intraday)")
    private Map<String, IntradayData> timeSeriesIntraday;
    
    @Data
    public static class MetaData {
        @JsonProperty("1. Information")
        private String information;
        
        @JsonProperty("2. Symbol")
        private String symbol;
        
        @JsonProperty("3. Last Refreshed")
        private String lastRefreshed;
    }
    
    @Data
    public static class DailyData {
        @JsonProperty("1. open")
        private String open;
        
        @JsonProperty("2. high")
        private String high;
        
        @JsonProperty("3. low")
        private String low;
        
        @JsonProperty("4. close")
        private String close;
        
        @JsonProperty("5. volume")
        private String volume;
    }
    
    @Data
    public static class IntradayData {
        @JsonProperty("1. open")
        private String open;
        
        @JsonProperty("2. high")
        private String high;
        
        @JsonProperty("3. low")
        private String low;
        
        @JsonProperty("4. close")
        private String close;
        
        @JsonProperty("5. volume")
        private String volume;
    }
}
