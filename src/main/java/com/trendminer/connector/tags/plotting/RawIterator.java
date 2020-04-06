package com.trendminer.connector.tags.plotting;

import com.trendminer.connector.tags.interpolation.InterpolationStrategy;
import com.trendminer.connector.tags.model.DataPoint;

import java.util.List;

public class RawIterator {
    private long startTs;
    private long endTs;

    private int currentOffset = 0;

    private boolean hasInterpolatedStart = false;
    private boolean hasInterpolatedEnd = false;
    private boolean interpolateStart = true;
    private boolean shouldInterpolateEnd = false;

    private DataPointIterator pointIterator;
    private InterpolationStrategy interpolator;

    public RawIterator(
            InterpolationStrategy interpolator, List<DataPoint> dataPoints, long startTs, long endTs) {
        this.startTs = startTs;
        this.endTs = endTs;
        this.pointIterator = new DataPointIterator(dataPoints);
        this.interpolator = interpolator;
        initialMoveTo(pointIterator, startTs);

        interpolateStart = !(pointIterator.hasValue() && pointIterator.getTs() == startTs);
    }

    public boolean hasNext() {
        if (interpolateStart && !hasInterpolatedStart) {
            return true;
        }

        if (hasMorePoints()) {
            return true;
        }

        if (!hasInterpolatedEnd) {
            if (pointIterator.hasValue() && pointIterator.getTs() == endTs) {
                shouldInterpolateEnd = false;
                hasInterpolatedEnd = true;
                return true;
            }
            shouldInterpolateEnd = true;
            return true;
        }
        return false;
    }

    public Integer next() {
        if (checkFirstPointNeedsToBeInterpolated()) {
            return currentOffset;
        }
        interpolateStart = false;
        if (checkIfEndIsReached()) {
            return currentOffset;
        }

        pointIterator.next();
        return 0;
    }

    private boolean checkIfEndIsReached() {
        if (shouldInterpolateEnd) {
            hasInterpolatedEnd = true;
            return true;
        }
        return false;
    }

    private boolean checkFirstPointNeedsToBeInterpolated() {
        if (interpolateStart && !hasInterpolatedStart) {
            hasInterpolatedStart = true;
            return true;
        }
        return false;
    }

    public double interpolateBefore(long ts) {
        if (!pointIterator.hasValue()) {
            return Double.NaN;
        }
        return interpolator.interpolate(ts, pointIterator);
    }

    public double getValue() {
        if (interpolateStart) {
            return interpolateBefore(startTs);
        }
        if (shouldInterpolateEnd) {
            return interpolateBefore(endTs);
        }
        return pointIterator.getPreviousValue();
    }

    public long getTs() {
        if (interpolateStart) {
            return startTs;
        }
        if (shouldInterpolateEnd) {
            return endTs;
        }
        return pointIterator.getPreviousTs();
    }

    private boolean hasMorePoints() {
        return pointIterator.hasValue() && pointIterator.getTs() < endTs;
    }

    private void initialMoveTo(DataPointIterator pointIterator, long testOffset) {
        pointIterator.hint();
        while (pointIterator.hasValue() && pointIterator.getTs() < testOffset) {
            pointIterator.next();
        }
    }
}
