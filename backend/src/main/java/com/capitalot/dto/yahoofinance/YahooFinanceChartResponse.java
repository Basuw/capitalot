package com.capitalot.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class YahooFinanceChartResponse {
    @JsonProperty("chart")
    private Chart chart;
    
    @Data
    public static class Chart {
        @JsonProperty("result")
        private List<Result> result;
        
        @JsonProperty("error")
        private Object error;
    }
    
    @Data
    public static class Result {
        @JsonProperty("meta")
        private Meta meta;
        
        @JsonProperty("timestamp")
        private List<Long> timestamp;
        
        @JsonProperty("indicators")
        private Indicators indicators;
    }
    
    @Data
    public static class Meta {
        @JsonProperty("currency")
        private String currency;

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("exchangeName")
        private String exchangeName;

        @JsonProperty("fullExchangeName")
        private String fullExchangeName;

        @JsonProperty("instrumentType")
        private String instrumentType;

        @JsonProperty("firstTradeDate")
        private Long firstTradeDate;

        @JsonProperty("regularMarketTime")
        private Long regularMarketTime;

        @JsonProperty("regularMarketPrice")
        private Double regularMarketPrice;

        @JsonProperty("regularMarketDayHigh")
        private Double regularMarketDayHigh;

        @JsonProperty("regularMarketDayLow")
        private Double regularMarketDayLow;

        @JsonProperty("regularMarketVolume")
        private Long regularMarketVolume;

        @JsonProperty("fiftyTwoWeekHigh")
        private Double fiftyTwoWeekHigh;

        @JsonProperty("fiftyTwoWeekLow")
        private Double fiftyTwoWeekLow;

        @JsonProperty("chartPreviousClose")
        private Double chartPreviousClose;

        @JsonProperty("longName")
        private String longName;

        @JsonProperty("shortName")
        private String shortName;

        @JsonProperty("gmtoffset")
        private Integer gmtoffset;

        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("previousClose")
        private Double previousClose;

        @JsonProperty("scale")
        private Integer scale;

        @JsonProperty("priceHint")
        private Integer priceHint;

        @JsonProperty("currentTradingPeriod")
        private TradingPeriod currentTradingPeriod;
    }
    
    @Data
    public static class TradingPeriod {
        @JsonProperty("pre")
        private Period pre;
        
        @JsonProperty("regular")
        private Period regular;
        
        @JsonProperty("post")
        private Period post;
    }
    
    @Data
    public static class Period {
        @JsonProperty("timezone")
        private String timezone;
        
        @JsonProperty("start")
        private Long start;
        
        @JsonProperty("end")
        private Long end;
        
        @JsonProperty("gmtoffset")
        private Integer gmtoffset;
    }
    
    @Data
    public static class Indicators {
        @JsonProperty("quote")
        private List<Quote> quote;
        
        @JsonProperty("adjclose")
        private List<AdjClose> adjclose;
    }
    
    @Data
    public static class Quote {
        @JsonProperty("open")
        private List<Double> open;
        
        @JsonProperty("high")
        private List<Double> high;
        
        @JsonProperty("low")
        private List<Double> low;
        
        @JsonProperty("close")
        private List<Double> close;
        
        @JsonProperty("volume")
        private List<Long> volume;
    }
    
    @Data
    public static class AdjClose {
        @JsonProperty("adjclose")
        private List<Double> adjclose;
    }
}
