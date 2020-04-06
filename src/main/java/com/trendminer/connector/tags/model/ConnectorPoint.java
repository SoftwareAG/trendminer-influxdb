package com.trendminer.connector.tags.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectorPoint {

    @JsonProperty("Ts")
    private final String ts;

    @JsonProperty("Value")
    private final String value;

    public ConnectorPoint(String ts, String value) {
        this.ts = ts;
        this.value = value;
    }

    public String getTs() {
        return ts;
    }

    public String getValue() {
        return value;
    }
}
