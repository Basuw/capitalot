package com.capitalot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlphaVantageOverviewResponse {
    @JsonProperty("Symbol") private String symbol;
    @JsonProperty("Name") private String name;
    @JsonProperty("Description") private String description;
    @JsonProperty("Exchange") private String exchange;
    @JsonProperty("Currency") private String currency;
    @JsonProperty("Country") private String country;
    @JsonProperty("Sector") private String sector;
    @JsonProperty("Industry") private String industry;
    @JsonProperty("Beta") private String beta;
    @JsonProperty("DividendPerShare") private String dividendPerShare;
    @JsonProperty("DividendYield") private String dividendYield;
    @JsonProperty("EPS") private String eps;
    @JsonProperty("PERatio") private String peRatio;
    @JsonProperty("MarketCapitalization") private String marketCapitalization;
    @JsonProperty("AnalystTargetPrice") private String analystTargetPrice;
    @JsonProperty("AnalystRatingStrongBuy") private String analystRatingStrongBuy;
    @JsonProperty("AnalystRatingBuy") private String analystRatingBuy;
    @JsonProperty("AnalystRatingHold") private String analystRatingHold;
    @JsonProperty("AnalystRatingSell") private String analystRatingSell;
    @JsonProperty("AnalystRatingStrongSell") private String analystRatingStrongSell;
    @JsonProperty("Note") private String note;
    @JsonProperty("Information") private String information;
}
