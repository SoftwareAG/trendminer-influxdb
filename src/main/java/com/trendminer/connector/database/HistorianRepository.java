package com.trendminer.connector.database;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface HistorianRepository extends CrudRepository<Historian, Integer> {
    Optional<Historian> findByName(String name);
}
