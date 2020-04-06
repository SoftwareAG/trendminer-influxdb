package com.trendminer.connector.tags;

import com.trendminer.connector.database.Historian;
import com.trendminer.connector.influx.model.Metric;
import com.trendminer.connector.tags.model.TagType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    @Cacheable(value = "tagCache", key = "{#historian.id, #tagName}")
    public Metric nameToMetric(Historian historian, String tagName, TagType tagType) {
        Metric metric = Metric.createMetricComplete(historian, tagName, tagType);
        return metric;
    }
}
