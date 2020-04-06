package com.trendminer.connector.tags;

import com.trendminer.connector.database.Historian;
import com.trendminer.connector.influx.util.UriBuilder;

import com.trendminer.connector.influx.model.Metric;
import com.trendminer.connector.influx.model.QueryResult;
import com.trendminer.connector.tags.model.TagDetailsDTO;
import com.trendminer.connector.tags.model.TagType;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.trendminer.connector.tags.model.TagType.*;
import static java.util.stream.Collectors.toList;

@Component
public class TimeSeriesDefinitionsOperation {

    private final RestTemplate restTemplate;
    private final UriBuilder uriBuilder;

    @Autowired
    public TimeSeriesDefinitionsOperation(RestTemplate restTemplate, UriBuilder uriBuilder) {
        this.restTemplate = restTemplate;
        this.uriBuilder = uriBuilder;
    }

    @Cacheable(value="tagCache")
    public Try<List<TagDetailsDTO>> getTimeSeriesDefinitions(Historian historian) {
        return Try.of(() -> {
            URI uri = uriBuilder.fromQuery(historian, "SHOW SERIES").build().toUri();
//            URI uri = uriBuilder.fromQuery(historian, "SHOW SERIES on ").build().toUri();
            QueryResult qr  = restTemplate.getForObject(uri, QueryResult.class);
            if (qr.getResults().size() != 1) {
                throw new Exception ("Wrong result set!");
            }
            List<List<Object>> rseries = qr.getResults().get(0).getSeries().get(0).getValues();

//            uri = uriBuilder.fromQuery(historian, "SHOW FIELD KEYS on ").build().toUri();
            uri = uriBuilder.fromQuery(historian, "SHOW FIELD KEYS").build().toUri();
            qr  = restTemplate.getForObject(uri, QueryResult.class);
            if (qr.getResults().size() != 1) {
                throw new Exception ("Wrong result set!");
            }
            List<QueryResult.Series> rfields = qr.getResults().get(0).getSeries();

            return StreamSupport.stream(rseries.spliterator(), false)
                    .map(serie -> Metric.createMetricPartial(serie.get(0).toString(), historian))
                    .flatMap(metric -> StreamSupport.stream(rfields.spliterator(), false)
                            .filter(measurement -> measurement.getName().equals(metric.getMeasurement()))
                            .flatMap(measurement -> StreamSupport.stream(measurement.getValues().spliterator(), false)
                                    .filter(this::canBeConvertedToTag)
                                    .map( field -> createTagDetails(metric, field)))
                            )
                    .collect(toList());
        });
    }

    private boolean canBeConvertedToTag(List<Object> field) {
        return "integer".equals(field.get(1).toString()) || "float".equals(field.get(1).toString()) || "boolean".equals(field.get(1).toString());
    }

    private TagDetailsDTO createTagDetails(Metric metric, List<Object> field) {
        return new TagDetailsDTO (metric.getName() +  field.get(0).toString(),
                null, metric.getHistorian().getId() ,
                null,  deriveTagTypeFromTypeId(field)
        );
    }

     private TagType deriveTagTypeFromTypeId(List<Object> field) {
         if ("integer".equals(field.get(0).toString())) {
             return DISCRETE;
         }
         if ("boolean".equals(field.get(0).toString())||
                 "string".equals(field.get(0).toString()) ) {
             return STRING;
         }
         return ANALOG;  // analog is default
     }
}
