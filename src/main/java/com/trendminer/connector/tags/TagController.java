package com.trendminer.connector.tags;

import com.google.common.base.Strings;
import com.trendminer.connector.common.HistorianNotFoundException;
import com.trendminer.connector.database.Historian;
import com.trendminer.connector.database.HistorianRepository;
import com.trendminer.connector.influx.util.UriBuilder;
import com.trendminer.connector.influx.model.Metric;
import com.trendminer.connector.influx.model.QueryResult;
import com.trendminer.connector.tags.model.*;
import com.trendminer.connector.tags.plotting.PlotService;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.trendminer.connector.tags.model.TagType.ANALOG;
import static com.trendminer.connector.tags.model.TagType.DISCRETE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RestController
public class TagController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagController.class);
    private final RestTemplate restTemplate;
    private final UriBuilder uriBuilder;
    private final PlotService plotService;
    private final TimeSeriesDefinitionsOperation timeSeriesDefinitionsOperation;
    private final HistorianRepository historianRepository;
    private final TagDetailsRepository tagDetailsRepository;
    private final TagService tagService;

    @Autowired
    public TagController(RestTemplate restTemplate,
                         UriBuilder uriBuilder,
                         PlotService plotService,
                         TagService tagService,
                         TimeSeriesDefinitionsOperation timeSeriesDefinitionsOperation,
                         HistorianRepository historianRepository, TagDetailsRepository tagDetailsRepository) {
        this.restTemplate = restTemplate;
        this.uriBuilder = uriBuilder;
        this.plotService = plotService;
        this.tagService = tagService;
        this.timeSeriesDefinitionsOperation = timeSeriesDefinitionsOperation;
        this.historianRepository = historianRepository;
        this.tagDetailsRepository = tagDetailsRepository;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong arguments")
    @ExceptionHandler(IllegalArgumentException.class)
    public void conflict() {
        // Nothing to do
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
        QueryResult result = fetchPointMaps(historian, metric, start, end);
        List<List<Object>> points = result.getResults().get(0).getSeries().get(0)
                .getValues();
        List<DataPoint> dataPoints = StreamSupport.stream(points.spliterator(), true)
                .filter(point -> point.size() == 2)
                .map(point -> createDataPoint(point, tagType))
                .collect(toList());

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
        QueryResult result  = fetchPointMaps(historian, metric, start, end);
        List<List<Object>> points = result.getResults().get(0).getSeries().get(0)
                .getValues();
        List<ConnectorPoint> connectorPoints = StreamSupport.stream(points.spliterator(), true)
                .filter(point -> point.size() == 2)
                .map(this::createDigitalConnectorPoint)
                .collect(toList());

        stopWatch.stop();
        LOGGER.info("Fetching points from {} to {} for {} took {} ms", start, end, metric, stopWatch.getTime());
        return connectorPoints;
    }

    private QueryResult fetchPointMaps(Historian historian, Metric metric, String start, String end) {
        String pointUri = createPointUri(historian, metric, start, end);
        return restTemplate.getForObject(pointUri, QueryResult.class);
    }

    private ConnectorPoint createDigitalConnectorPoint(List<Object> point) {
        return new ConnectorPoint((String) point.get(0), (String) point.get(1));
    }

    private String createPointUri(Historian historian, Metric metric, String start, String end) {
        String whereClause = "";
        for ( Metric.InfluxTag t: metric.getTags()){
            whereClause = whereClause + t.getName() + "=" + "'" + t.getValue() + "'";
            whereClause = whereClause + " AND " +  "time >= '" + start + "' AND  time < '" +end + "'";
        }
        return uriBuilder.fromQuery(historian,
                format("SELECT time,%s FROM %s WHERE %s"
                    ,metric.getField().getName(),metric.getMeasurement(), whereClause))
                .build().toUriString();
    }

    private ConnectorPoint createConnectorPoint(DataPoint dataPoint, TagType tagType) {
        String value = createPointValue(dataPoint, tagType);
        return new ConnectorPoint(dataPoint.getTs().format(DateTimeFormatter.ISO_INSTANT), value);
    }

    private String createPointValue(DataPoint dataPoint, TagType tagType) {
        if (tagType == DISCRETE) {
            return format(Locale.US,"%d", (int) dataPoint.getValue());
        }
        return format(Locale.US,"%f", dataPoint.getValue());
    }

    private DataPoint createDataPoint(List<Object> point, TagType tagType) {
        if (tagType == ANALOG) {
            return new DataPoint(Instant.parse((String) point.get(0)), (Double) point.get(1));
        }
        return new DataPoint(Instant.parse((String) point.get(0)), (Integer) point.get(1));
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
