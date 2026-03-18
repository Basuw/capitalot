package com.capitalot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinnhubProfileResponse {
    @JsonProperty("country")
    private String country;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("finnhubIndustry")
    private String finnhubIndustry;

    @JsonProperty("ipo")
    private String ipo;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("marketCapitalization")
    private Double marketCapitalization;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shareOutstanding")
    private Double shareOutstanding;

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("weburl")
    private String weburl;
}
