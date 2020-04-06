package com.trendminer.connector.tags.interpolation;

import com.trendminer.connector.tags.plotting.DataPointIterator;

public class LinearInterpolator implements InterpolationStrategy {

    public double interpolate(long ts, DataPointIterator pointIterator) {
        if (!pointIterator.hasValue()) {
            return Double.NaN;
        }

        if (!pointIterator.hasPrevious()) {
            if (ts == pointIterator.getTs()) {
                return pointIterator.getValue();
            }
            return Double.NaN;
        }

        long afterOffset = pointIterator.getTs();
        long beforeOffset = pointIterator.getPreviousTs();
        double afterValue = pointIterator.getValue();
        double beforeValue = pointIterator.getPreviousValue();

        if (ts == beforeOffset) {
            return beforeValue;
        }

        if (ts == afterOffset) {
            return afterValue;
        }

        return beforeValue
                + (afterValue - beforeValue)
                * ((double) (ts - beforeOffset))
                / (afterOffset - beforeOffset);
    }
}
