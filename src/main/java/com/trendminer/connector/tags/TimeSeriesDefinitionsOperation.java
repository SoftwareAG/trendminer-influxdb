package com.trendminer.connector.tags;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.trendminer.connector.database.Historian;
import com.trendminer.connector.tags.model.TagDetailsDTO;
import com.trendminer.connector.tags.model.TagType;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static com.trendminer.connector.tags.model.TagType.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Component
public class TimeSeriesDefinitionsOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesDefinitionsOperation.class);

    @Autowired
    public TimeSeriesDefinitionsOperation() {
    }

    @Cacheable(value = "tagCache")
    public Try<List<TagDetailsDTO>> getTimeSeriesDefinitions(Historian historian) {
        return Try.of(() -> {
            InfluxDBClient influxDBClient = InfluxDBClientFactory.create(historian.getDataSource(),
                    historian.getPassword().toCharArray());
            String flux = format("from(bucket: \"%s\") " +
                    "|> range(start: -10y) " +
                    "|> last() " +
                    "|> drop(columns: [\"_start\", \"_stop\", \"_time\"]) " +
                    "|> group()", historian.getName());
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, historian.getPrefix());
            if (tables.size() != 1) {
                LOGGER.error("Number of result tables should be 1 but it is:" + tables.size());
                throw new IllegalArgumentException();
            }

            List<FluxRecord> tags = tables.get(0).getRecords();
            List<TagDetailsDTO> result = tags.parallelStream()
                    .map(record -> createTagDetails(historian, record))
                    .collect(toList());
            influxDBClient.close();
            return result;
        });
    }

    private TagDetailsDTO createTagDetails(Historian historian, FluxRecord fluxRecord) {
        Map<String, Object> fluxRecordValues = fluxRecord.getValues();
        String _field = "";
        String _measurement = "";
        StringBuilder _tags = new StringBuilder();
        TagType tagType = ANALOG;

        for (Map.Entry<String, Object> entry : fluxRecordValues.entrySet()) {
            switch (entry.getKey()) {
                case "_measurement":
                    _measurement = entry.getValue().toString();
                    break;
                case "_value":
                    tagType = deriveTagTypeFromTypeId(entry.getValue());
                    break;
                case "_field":
                    _field = entry.getValue().toString();
                    break;
                case "table":
                case "result":
                    break; // ignore
                default:
                    _tags.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(",");
                    break; // ignore
            }
        }
        _tags = new StringBuilder(StringUtils.trimTrailingCharacter(_tags.toString(), ','));
        return new TagDetailsDTO(_measurement + "," + _tags + "," + _field, "Tag for measurement:" + _measurement, historian.getId(), "no_unit", tagType);
    }

    private TagType deriveTagTypeFromTypeId(Object field) {
        if (field instanceof Double) {
            return ANALOG;
        } else if (field instanceof Float) {
            return ANALOG;
        } else if (field instanceof Boolean) {
            return STRING;
        } else if (field instanceof String) {
            return STRING;
        } else if (field instanceof Integer) {
            return DISCRETE;  // analog is default
        }
        return ANALOG;  // analog is default
    }
}
