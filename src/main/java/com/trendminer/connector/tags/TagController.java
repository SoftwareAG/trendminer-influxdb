package com.trendminer.connector.tags;

import com.google.common.base.Strings;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.trendminer.connector.common.HistorianNotFoundException;
import com.trendminer.connector.database.Historian;
import com.trendminer.connector.database.HistorianRepository;
import com.trendminer.connector.influx.model.Metric;
import com.trendminer.connector.tags.model.*;
import com.trendminer.connector.tags.plotting.PlotService;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.trendminer.connector.tags.model.TagType.ANALOG;
import static com.trendminer.connector.tags.model.TagType.DISCRETE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RestController
public class TagController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagController.class);
    private final PlotService plotService;
    private final TimeSeriesDefinitionsOperation timeSeriesDefinitionsOperation;
    private final HistorianRepository historianRepository;
    private final TagDetailsRepository tagDetailsRepository;
    private final TagService tagService;

    @Autowired
    public TagController(PlotService plotService,
                         TagService tagService,
                         TimeSeriesDefinitionsOperation timeSeriesDefinitionsOperation,
                         HistorianRepository historianRepository, TagDetailsRepository tagDetailsRepository) {
        this.plotService = plotService;
        this.tagService = tagService;
        this.timeSeriesDefinitionsOperation = timeSeriesDefinitionsOperation;
        this.historianRepository = historianRepository;
        this.tagDetailsRepository = tagDetailsRepository;
    }

    @GetMapping(value = "/v2/tags")
    public List<TagDetailsDTO> listTags(@RequestParam("historianName") String historianName) {
        if (Strings.isNullOrEmpty(historianName)) {
            throw new IllegalArgumentException();
        }

        return historianRepository.findByName(historianName)
                .map(timeSeriesDefinitionsOperation::getTimeSeriesDefinitions)
                .orElseThrow(HistorianNotFoundException::new)
                .get();
    }

    @GetMapping("/v2/tags/rawvalues")
    public Iterable<ConnectorPoint> getRawValues(
            @RequestParam("tagName") String tagName,
            @RequestParam("startDate") String start,
            @RequestParam("endDate") String end) {
        return emptyList();
    }

    @PostMapping("/v2/tags/lastvalues")
    public Iterable<ConnectorPoint> getLastValues() {
        return emptyList();
    }

    @GetMapping("/v2/tags/digitalstates")
    public Iterable<DigitalStateDTO> getDigitalStates(@RequestParam("tagName") String tagName) {
        return emptyList();
    }

    @GetMapping("/v2/tags/plotvalues")
    public Iterable<ConnectorPoint> getPlotValues(
            @RequestParam("tagName") String tagName,
            @RequestParam("tagType") TagType tagType,
            @RequestParam("interpolationType") InterpolationType interpolationType,
            @RequestParam("startDate") String start,
            @RequestParam("endDate") String end,
            @RequestParam("numberOfIntervals") int numberOfIntervals) {
        TagDetails tagDetails = fetchTagDetails(tagName);
        Historian historian = fetchHistorian(tagDetails);
        Metric metric = tagService.nameToMetric(historian, tagName, tagType);
        return fetchPoints(historian, metric, tagType, interpolationType, start, end, numberOfIntervals);
    }

    @GetMapping("/v2/tags/indexvalues")
    public Iterable<ConnectorPoint> getIndexValues(
            @RequestParam("tagName") String tagName,
            @RequestParam("tagType") TagType tagType,
            @RequestParam("interpolationType") InterpolationType interpolationType,
            @RequestParam("startDate") String start,
            @RequestParam("endDate") String end,
            @RequestParam("numberOfIntervals") int numberOfIntervals) {
        TagDetails tagDetails = fetchTagDetails(tagName);
        Historian historian = fetchHistorian(tagDetails);
        Metric metric = tagService.nameToMetric(historian, tagName, tagType);
        return fetchPoints(historian, metric, tagType, interpolationType, start, end, numberOfIntervals);
    }

    private Iterable<ConnectorPoint> fetchPoints(Historian historian,
                                                 Metric metric,
                                                 TagType tagType,
                                                 InterpolationType interpolationType,
                                                 String start,
                                                 String end,
                                                 int numberOfIntervals) {
        if (tagType == ANALOG || tagType == DISCRETE) {
            return fetchAnalogPoints(historian, metric, start, end, numberOfIntervals, interpolationType, tagType);
        }
        return fetchStringPoints(historian, metric, start, end);
    }

    private Iterable<ConnectorPoint> fetchAnalogPoints(Historian historian,
                                                       Metric metric,
                                                       String start,
                                                       String end,
                                                       int numberOfIntervals,
                                                       InterpolationType interpolationType,
                                                       TagType tagType) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String flux = createPointUri(historian, metric, start, end);
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(historian.getDataSource(),
                historian.getPassword().toCharArray());
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, historian.getPrefix());
        List<FluxRecord> points = new ArrayList<FluxRecord>();
        if (tables.size() != 1) {
            LOGGER.warn("No data found in the specified time interval.");
        } else  {
            points = tables.get(0).getRecords();
        }

        List<DataPoint> dataPoints = points.parallelStream()
                .map(point -> createDataPoint(point, tagType))
                .collect(toList());
        influxDBClient.close();

        List<ConnectorPoint> connectorPoints = plotService.getPlottableData(dataPoints, Instant.parse(start), Instant.parse(end), numberOfIntervals, interpolationType)
                .stream()
                .map(dataPoint -> createConnectorPoint(dataPoint, tagType))
                .collect(toList());
        stopWatch.stop();

        LOGGER.info("Fetching points from {} to {} for {} took {} ms", start, end, metric, stopWatch.getTime());
        return connectorPoints;
    }

    private Iterable<ConnectorPoint> fetchStringPoints(Historian historian, Metric metric, String start, String end) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String flux = createPointUri(historian, metric, start, end);
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(historian.getDataSource(),
                historian.getPassword().toCharArray());
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, historian.getPrefix());
        if (tables.size() != 1) {
            LOGGER.error("Number of result tables should be 1 but it is:" + tables.size());
            throw new IllegalArgumentException();
        }
        List<FluxRecord> points = tables.get(0).getRecords();
        List<ConnectorPoint> connectorPoints = points.parallelStream()
                .map(this::createDigitalConnectorPoint)
                .collect(toList());
        influxDBClient.close();
        stopWatch.stop();
        LOGGER.info("Fetching points from {} to {} for {} took {} ms", start, end, metric, stopWatch.getTime());
        return connectorPoints;
    }

    private ConnectorPoint createDigitalConnectorPoint(FluxRecord point) {
        return new ConnectorPoint((String) point.getValueByKey("_time"), (String) point.getValueByKey("_value"));
    }

    private String createPointUri(Historian historian, Metric metric, String start, String end) {
        String flux_query = format("from(bucket: \"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\") " +
                        "|> filter(fn: (r) => r._field == \"%s\") ",
                historian.getName(),
                start, end,
                metric.getMeasurement(), metric.getField().getName());

        String filterClause = metric.getTags().stream()
                .map(t -> format("|> filter(fn: (r) => r.%s == \"%s\")", t.getName(), t.getValue()))
                .collect(Collectors.joining(" "));

        return flux_query + filterClause;
    }

    private ConnectorPoint createConnectorPoint(DataPoint dataPoint, TagType tagType) {
        String value = createPointValue(dataPoint, tagType);
        return new ConnectorPoint(dataPoint.getTs().format(DateTimeFormatter.ISO_INSTANT), value);
    }

    private String createPointValue(DataPoint dataPoint, TagType tagType) {
        if (tagType == DISCRETE) {
            return format(Locale.US, "%d", (int) dataPoint.getValue());
        }
        return format(Locale.US, "%f", dataPoint.getValue());
    }

    private DataPoint createDataPoint(FluxRecord point, TagType tagType) {
        if (tagType == ANALOG) {
            return new DataPoint((Instant) point.getValueByKey("_time"), (Double) point.getValueByKey("_value"));
        }
        return new DataPoint((Instant) point.getValueByKey("_time"), (Integer) point.getValueByKey("_value"));
    }

    private Historian fetchHistorian(TagDetails tagDetails) {
        Optional<Historian> historian = historianRepository.findById(tagDetails.getHistorian());
        if (!historian.isPresent()) {
            throw new HistorianNotFoundException();
        }
        return historian.get();
    }

    private TagDetails fetchTagDetails(String tagName) {
        Optional<TagDetails> searchingTagDetails = tagDetailsRepository.findByName(tagName);
        if (!searchingTagDetails.isPresent()) {
            throw new TagNotFoundException();
        }

        return searchingTagDetails.get();
    }
}
