package com.trendminer.connector.tags.interpolation;

import com.trendminer.connector.tags.plotting.DataPointIterator;

public interface InterpolationStrategy {
    double interpolate(long ts, DataPointIterator pointIterator);
}
