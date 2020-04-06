package com.trendminer.connector.database;

import com.trendminer.connector.common.HistorianNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.StreamSupport;

import static com.trendminer.connector.database.HistorianMapper.*;
import static java.util.stream.Collectors.toList;

@RestController
public class DatabaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseController.class);

    private final HistorianRepository historianRepository;
    private final TagSync tagSync;

    @Autowired
    public DatabaseController(HistorianRepository historianRepository, TagSync tagSync) {
        this.historianRepository = historianRepository;
        this.tagSync = tagSync;
    }

    @GetMapping("/database")
    public Iterable<HistorianDTO> getHistorians() {
        return StreamSupport.stream(historianRepository.findAll().spliterator(), false)
                .map(HistorianMapper::toDto)
                .collect(toList());
    }

    @GetMapping("/database/{id}")
    public HistorianDTO getHistorian(@PathVariable("id") int id) {
        return historianRepository
                .findById(id)
                .map(HistorianMapper::toDto)
                .orElseThrow(HistorianNotFoundException::new);
    }

    @PostMapping(value = "/database", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int createHistorian(@RequestBody HistorianDTO historianDTO) {
        Historian historian = historianRepository.save(toEntityWithoutId(historianDTO));
        tagSync.syncTagsAsync(historian);
        return historian.getId();
    }

    @PutMapping(
            value = "/database/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public HistorianDTO updateHistorian(
            @PathVariable("id") int id, @RequestBody HistorianDTO historianDTO) {
        if (id != historianDTO.getId()) {
            throw new MismatchedIdException();
        }
        Historian historian = historianRepository.save(toEntity(historianDTO));
        tagSync.syncTagsAsync(historian);
        return toDto(historian);
    }

    @DeleteMapping("/database/{id}")
    public ResponseEntity<Void> deleteHistorian(@PathVariable("id") int id) {
        try {
            historianRepository
                    .findById(id)
                    .ifPresent(
                            historian -> {
                                tagSync.removeTags(historian);
                                historianRepository.delete(historian);
                            });
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not find database with {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/database/{id}/test")
    public ResponseEntity<Boolean> testConnection(@PathVariable("id") int id) {
        // TODO build connection test
        return ResponseEntity.ok(true);
    }
}
