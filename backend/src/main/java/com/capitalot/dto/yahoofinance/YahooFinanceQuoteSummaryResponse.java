package com.capitalot.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YahooFinanceQuoteSummaryResponse {
    @JsonProperty("quoteSummary")
    private QuoteSummary quoteSummary;
    
    @Data
    public static class QuoteSummary {
        @JsonProperty("result")
        private List<Result> result;
        
        @JsonProperty("error")
        private Object error;
    }
    
    @Data
    public static class Result {
        @JsonProperty("assetProfile")
        private AssetProfile assetProfile;
        
        @JsonProperty("financialData")
        private FinancialData financialData;
        
        @JsonProperty("defaultKeyStatistics")
        private DefaultKeyStatistics defaultKeyStatistics;
        
        @JsonProperty("calendarEvents")
        private CalendarEvents calendarEvents;
        
        @JsonProperty("majorHoldersBreakdown")
        private MajorHoldersBreakdown majorHoldersBreakdown;
    }
    
    @Data
    public static class AssetProfile {
        @JsonProperty("sector")
        private String sector;
        
        @JsonProperty("industry")
        private String industry;
        
        @JsonProperty("longBusinessSummary")
        private String longBusinessSummary;
        
        @JsonProperty("fullTimeEmployees")
        private Integer fullTimeEmployees;
    }
    
    @Data
    public static class FinancialData {
        @JsonProperty("currentPrice")
        private ValueWrapper currentPrice;

        @JsonProperty("totalCash")
        private ValueWrapper totalCash;
        
        @JsonProperty("totalDebt")
        private ValueWrapper totalDebt;
        
        @JsonProperty("grossMargins")
        private ValueWrapper grossMargins;
        
        @JsonProperty("revenuePerShare")
        private ValueWrapper revenuePerShare;
    }
    
    @Data
    public static class DefaultKeyStatistics {
        @JsonProperty("trailingPE")
        private ValueWrapper trailingPE;
        
        @JsonProperty("beta")
        private ValueWrapper beta;
    }
    
    @Data
    public static class CalendarEvents {
        @JsonProperty("exDividendDate")
        private ValueWrapper exDividendDate;
    }
    
    @Data
    public static class MajorHoldersBreakdown {
        @JsonProperty("insidersPercent")
        private ValueWrapper insidersPercent;
        
        @JsonProperty("institutionsPercent")
        private ValueWrapper institutionsPercent;
    }
    
    @Data
    public static class ValueWrapper {
        @JsonProperty("raw")
        private Double raw;
        
        @JsonProperty("fmt")
        private String fmt;
    }
}
