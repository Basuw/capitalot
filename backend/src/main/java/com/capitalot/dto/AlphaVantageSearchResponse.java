package com.capitalot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AlphaVantageSearchResponse {
    @JsonProperty("bestMatches")
    private List<Match> bestMatches;

    @JsonProperty("Information")
    private String information;

    @JsonProperty("Note")
    private String note;

    @Data
    public static class Match {
        @JsonProperty("1. symbol")
        private String symbol;

        @JsonProperty("2. name")
        private String name;

        @JsonProperty("3. type")
        private String type;

        @JsonProperty("4. region")
        private String region;

        @JsonProperty("5. marketOpen")
        private String marketOpen;

        @JsonProperty("6. marketClose")
        private String marketClose;

        @JsonProperty("7. timezone")
        private String timezone;

        @JsonProperty("8. currency")
        private String currency;

        @JsonProperty("9. matchScore")
        private String matchScore;
    }
}
