package com.trendminer.connector.tags.interpolation;

import com.trendminer.connector.tags.plotting.DataPointIterator;

public class SteppedInterpolator implements InterpolationStrategy {

    public double interpolate(long ts, DataPointIterator pointIterator) {
        if (!pointIterator.hasPrevious()) {
            return Double.NaN;
        }
        return pointIterator.getPreviousValue();
    }
}
