package com.trendminer.connector.tags.plotting;

import com.trendminer.connector.tags.interpolation.InterpolationStrategy;
import com.trendminer.connector.tags.interpolation.InterpolatorFactory;
import com.trendminer.connector.tags.model.DataPoint;
import com.trendminer.connector.tags.model.InterpolationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlotService {

    private final InterestingPointFinder interestingPointFinder;

    @Autowired
    public PlotService(InterestingPointFinder interestingPointFinder) {
        this.interestingPointFinder = interestingPointFinder;
    }

    public List<DataPoint> getPlottableData(
            List<DataPoint> dataPoints,
            Instant startDate,
            Instant endDate,
            int numberOfIntervals,
            InterpolationType interpolationType) {
        List<DataPoint> results = new ArrayList<>();

        InterpolationStrategy interpolator = InterpolatorFactory.getInterpolator(interpolationType);
        RawIterator values =
                new RawIterator(interpolator, dataPoints, startDate.toEpochMilli(), endDate.toEpochMilli());

        values.next();

        long stepMillis = calculateStep(startDate, endDate, numberOfIntervals);
        for (long intervalStart = startDate.toEpochMilli();
             intervalStart < endDate.toEpochMilli();
             intervalStart += stepMillis) {
            results.addAll(
                    interestingPointFinder.find(values, intervalStart, intervalStart + stepMillis));
        }
        DataPoint lastPoint = interpolateLastPoint(values, endDate);
        results.add(lastPoint);

        return removeSameValues(removeNaNValues(results));
    }

    private List<DataPoint> removeNaNValues(List<DataPoint> points) {
        List<DataPoint> results = new ArrayList<>();
        for (DataPoint point : points) {
            if (!Double.isNaN(point.getValue())) {
                results.add(point);
            }
        }
        return results;
    }

    private List<DataPoint> removeSameValues(List<DataPoint> points) {
        if (points.size() < 2) {
            return points;
        }
        List<DataPoint> results = new ArrayList<>();
        for (DataPoint point : points) {
            if (isSameAsTwoBefore(results, point.getValue())) {
                results.remove(results.size() - 1);
            }
            results.add(point);
        }
        return results;
    }

    private long calculateStep(Instant startDate, Instant endDate, int numberOfIntervals) {
        return ((endDate.toEpochMilli() - startDate.toEpochMilli()) / numberOfIntervals);
    }

    private DataPoint interpolateLastPoint(RawIterator values, Instant endDate) {
        return new DataPoint(endDate, values.interpolateBefore(endDate.toEpochMilli()));
    }

    private boolean isSameAsTwoBefore(List<DataPoint> results, double value) {
        if (results.size() < 2) {
            return false;
        }
        double previousValue = results.get(results.size() - 1).getValue();
        double earlierValue = results.get(results.size() - 2).getValue();
        return Double.compare(previousValue, value) == 0 && Double.compare(earlierValue, value) == 0;
    }
}
