package com.trendminer.connector.database;

import com.trendminer.connector.tags.TagDetailsRepository;
import com.trendminer.connector.tags.TimeSeriesDefinitionsOperation;
import com.trendminer.connector.tags.model.TagDetails;
import com.trendminer.connector.tags.model.TagDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@Component
public class TagSync {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagSync.class);

    private TagDetailsRepository repository;
    private HistorianRepository historianRepository;
    private TimeSeriesDefinitionsOperation timeSeriesDefinitionsOperation;

    @Autowired
    public TagSync(
            TagDetailsRepository repository,
            HistorianRepository historianRepository,
            TimeSeriesDefinitionsOperation timeSeriesDefinitionsOperation) {
        this.repository = repository;
        this.historianRepository = historianRepository;
        this.timeSeriesDefinitionsOperation = timeSeriesDefinitionsOperation;
    }

    CompletableFuture<Void> syncTagsAsync(Historian historian) {
        return CompletableFuture.runAsync(() -> syncTagsBlocking(historian));
    }

    @Scheduled(fixedDelay = 3_600_000)
    void scheduledUpdateOfTags() {
        historianRepository.findAll().forEach(this::syncTagsBlocking);
    }

    private void syncTagsBlocking(Historian historian) {
        LOGGER.info("Syncing tags");
        timeSeriesDefinitionsOperation
                .getTimeSeriesDefinitions(historian)
                .onSuccess(timeSeriesDefinitions -> updateTags(historian, timeSeriesDefinitions));
    }

    private void updateTags(Historian historian, List<TagDetailsDTO> timeSeriesDefinitions) {
        LOGGER.info("All time series definitions fetched; start storing");
        List<TagDetails> details =
                timeSeriesDefinitions.stream()
                        .map(dto -> new TagDetails(dto.getName(), dto.getHistorian(), dto.getType()))
                        .collect(toList());
        repository.deleteTagDetailsByHistorian(historian.getId());
        repository.saveAll(details);
        LOGGER.info("Tags synced");
    }

    void removeTags(Historian historian) {
        repository.deleteTagDetailsByHistorian(historian.getId());
    }
}
