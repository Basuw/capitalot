package com.capitalot.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YahooFinanceSearchResponse {
    @JsonProperty("quotes")
    private List<Quote> quotes;
    
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
    }
}
