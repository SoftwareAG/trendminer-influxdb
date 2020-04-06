package com.trendminer.connector.tags.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DigitalStateDTO {

    @JsonProperty("Code")
    private final int code;

    @JsonProperty("Offset")
    private final int offset;

    @JsonProperty("Name")
    private final String name;

    public DigitalStateDTO(int code, int offset, String name) {
        this.code = code;
        this.offset = offset;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public int getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }
}
