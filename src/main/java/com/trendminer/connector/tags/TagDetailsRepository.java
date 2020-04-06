package com.trendminer.connector.tags;

import com.trendminer.connector.tags.model.TagDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TagDetailsRepository extends JpaRepository<TagDetails, Integer> {
    @Transactional
    void deleteTagDetailsByHistorian(int historian);

    Iterable<TagDetails> findAllByHistorian(int historian);

    Optional<TagDetails> findByName(String name);
}
