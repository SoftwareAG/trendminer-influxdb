package com.trendminer.connector.tags.plotting;

import com.trendminer.connector.tags.model.DataPoint;

import java.util.List;

public class DataPointIterator {
    private final List<DataPoint> points;
    private int pointer = 0;

    public DataPointIterator(List<DataPoint> points) {
        this.points = points;
    }

    public long getTs() {
        return points.get(pointer).getTs().toInstant().toEpochMilli();
    }

    public long getPreviousTs() {
        return points.get(pointer - 1).getTs().toInstant().toEpochMilli();
    }

    public double getValue() {
        return points.get(pointer).getValue();
    }

    public double getPreviousValue() {
        return points.get(pointer - 1).getValue();
    }

    public boolean hasPrevious() {
        return pointer != 0;
    }

    public boolean hasValue() {
        return pointer < points.size();
    }

    public void next() {
        pointer++;
    }

    public void hint() {
        pointer = 0;
    }
}
