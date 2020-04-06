package com.trendminer.connector.tags.plotting;

public class FinderContext {

    private double max = Double.NEGATIVE_INFINITY;
    private double min = Double.POSITIVE_INFINITY;
    private double first = Double.NEGATIVE_INFINITY;
    private double last = Double.POSITIVE_INFINITY;
    private long maxTs;
    private long minTs;
    private long firstTs;
    private long lastTs;

    public FinderContext(long startTs) {
        maxTs = startTs;
        minTs = startTs;
        firstTs = startTs;
        lastTs = startTs;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public double getLast() {
        return last;
    }

    public void setLast(double last) {
        this.last = last;
    }

    public long getMaxTs() {
        return maxTs;
    }

    public void setMaxTs(long maxTs) {
        this.maxTs = maxTs;
    }

    public long getMinTs() {
        return minTs;
    }

    public void setMinTs(long minTs) {
        this.minTs = minTs;
    }

    public long getFirstTs() {
        return firstTs;
    }

    public void setFirstTs(long firstTs) {
        this.firstTs = firstTs;
    }

    public long getLastTs() {
        return lastTs;
    }

    public void setLastTs(long lastTs) {
        this.lastTs = lastTs;
    }
}
