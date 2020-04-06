package com.trendminer.connector.tags.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DataPoint {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private double offset;
    private ZonedDateTime ts;
    private double value;

    public DataPoint() {
    }

    public DataPoint(ZonedDateTime ts, double value) {
        this.ts = ts;
        this.value = value;
    }

    public DataPoint(ZonedDateTime ts, double value, double offset) {
        this.ts = ts;
        this.value = value;
        this.offset = offset;
    }

    public DataPoint(Instant ts, double value) {
        this(ts.atZone(ZoneId.systemDefault()), value);
    }

    public DataPoint(Instant ts, double value, double offset) {
        this(ts.atZone(ZoneId.systemDefault()), value, offset);
    }

    public DataPoint(OffsetDateTime ts, double value) {
        this(Instant.from(ts), value);
    }

    @JsonFormat(pattern = DATE_PATTERN)
    public ZonedDateTime getTs() {
        return ts;
    }

    public void setTs(ZonedDateTime ts) {
        this.ts = ts;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DataPoint) && EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
