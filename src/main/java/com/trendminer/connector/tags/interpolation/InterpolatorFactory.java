package com.trendminer.connector.tags.interpolation;

import com.trendminer.connector.tags.model.InterpolationType;

public class InterpolatorFactory {

    public static InterpolationStrategy getInterpolator(InterpolationType interpolationType) {
        switch (interpolationType) {
            case STEPPED:
                return new SteppedInterpolator();
            case LINEAR:
            default:
                return new LinearInterpolator();
        }
    }
}
