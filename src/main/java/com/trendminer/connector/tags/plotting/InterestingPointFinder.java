package com.trendminer.connector.tags.plotting;

import com.trendminer.connector.tags.model.DataPoint;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterestingPointFinder {

    public List<DataPoint> find(RawIterator values, long startTs, long endTs) {
        FinderContext context = new FinderContext(startTs);
        findExtremes(values, context, endTs);
        return saveResults(context);
    }

    private void findExtremes(RawIterator values, FinderContext context, long endTs) {
        boolean isFirstPointSet = false;

        while (values.getTs() < endTs) {
            double value = values.getValue();
            long ts = values.getTs();

            if (!isFirstPointSet) {
                isFirstPointSet = true;
                context.setFirst(values.getValue());
                context.setFirstTs(values.getTs());
            }

            if (value > context.getMax()) {
                context.setMax(value);
                context.setMaxTs(ts);
            }
            if (value < context.getMin()) {
                context.setMin(value);
                context.setMinTs(ts);
            }

            context.setLast(value);
            context.setLastTs(ts);
            if (!values.hasNext()) {
                break;
            }
            values.next();
        }
    }

    private List<DataPoint> saveResults(FinderContext context) {
        List<DataPoint> results = new ArrayList<>();

        if (hasResults(context.getMax())) {
            if (Double.compare(context.getMin(), context.getMax()) == 0) {
                addFirstPointIfDifferent(results, context, context.getMin());
                results.add(new DataPoint(Instant.ofEpochMilli(context.getMinTs()), context.getMin()));
                addLastPointIfDifferent(results, context, context.getMin());
                return results;
            }
            if (context.getMaxTs() > context.getMinTs()) {
                addFirstPointIfDifferent(results, context, context.getMin());
                results.add(new DataPoint(Instant.ofEpochMilli(context.getMinTs()), context.getMin()));
                results.add(new DataPoint(Instant.ofEpochMilli(context.getMaxTs()), context.getMax()));
                addLastPointIfDifferent(results, context, context.getMax());
                return results;
            }
            addFirstPointIfDifferent(results, context, context.getMax());
            results.add(new DataPoint(Instant.ofEpochMilli(context.getMaxTs()), context.getMax()));
            results.add(new DataPoint(Instant.ofEpochMilli(context.getMinTs()), context.getMin()));
            addLastPointIfDifferent(results, context, context.getMin());
        }
        return results;
    }

    private boolean hasResults(double max) {
        return Double.compare(max, Double.NEGATIVE_INFINITY) != 0;
    }

    private void addFirstPointIfDifferent(
            List<DataPoint> results, FinderContext context, double earliestExtremaValue) {
        if (Double.compare(earliestExtremaValue, context.getFirst()) == 0) {
            return;
        }

        results.add(new DataPoint(Instant.ofEpochMilli(context.getFirstTs()), context.getFirst()));
    }

    private void addLastPointIfDifferent(
            List<DataPoint> results, FinderContext context, double latestExtremaValue) {
        if (Double.compare(latestExtremaValue, context.getLast()) == 0) {
            return;
        }

        results.add(new DataPoint(Instant.ofEpochMilli(context.getLastTs()), context.getLast()));
    }
}
