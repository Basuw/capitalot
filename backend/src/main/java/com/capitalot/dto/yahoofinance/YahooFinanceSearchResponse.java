package com.capitalot.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YahooFinanceSearchResponse {
    @JsonProperty("quotes")
    private List<Quote> quotes;
    
    @JsonProperty("news")
    private List<News> news;
    
    @Data
    public static class Quote {
        @JsonProperty("symbol")
        private String symbol;
        
        @JsonProperty("shortname")
        private String shortname;
        
        @JsonProperty("longname")
        private String longname;
        
        @JsonProperty("exchange")
        private String exchange;
        
        @JsonProperty("quoteType")
        private String quoteType;
        
        @JsonProperty("typeDisp")
        private String typeDisp;
        
        @JsonProperty("index")
        private String index;
        
        @JsonProperty("score")
        private Double score;

        @JsonProperty("sector")
        private String sector;

        @JsonProperty("industry")
        private String industry;
    }

    @Data
    public static class News {
        @JsonProperty("uuid")
        private String uuid;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("publisher")
        private String publisher;
        
        @JsonProperty("link")
        private String link;
        
        @JsonProperty("providerPublishTime")
        private Long providerPublishTime;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("thumbnail")
        private Thumbnail thumbnail;
    }

    @Data
    public static class Thumbnail {
        @JsonProperty("resolutions")
        private List<Resolution> resolutions;
    }

    @Data
    public static class Resolution {
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("width")
        private Integer width;
        
        @JsonProperty("height")
        private Integer height;
        
        @JsonProperty("tag")
        private String tag;
    }
}
