package com.capitalot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class FinnhubSearchResponse {
    @JsonProperty("count")
    private Integer count;

    @JsonProperty("result")
    private List<Result> result;

    @Data
    public static class Result {
        @JsonProperty("description")
        private String description;

        @JsonProperty("displaySymbol")
        private String displaySymbol;

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("type")
        private String type;
    }
}
