package com.trendminer.connector.database;

public class HistorianMapper {

    static HistorianDTO toDto(Historian historian) {
        return new HistorianDTO(
                historian.getId(),
                historian.getName(),
                historian.getPrefix(),
                historian.getProvider(),
                historian.getDataSource(),
                historian.getUserId(),
                "*********",
                historian.getVersion());
    }

    static Historian toEntity(HistorianDTO dto) {
        Historian historian =
                new Historian(
                        dto.getName(),
                        dto.getPrefix(),
                        dto.getProvider(),
                        dto.getDataSource(),
                        dto.getUserId(),
                        dto.getPassword(),
                        dto.getVersion());
        historian.setId(dto.getId());
        return historian;
    }

    static Historian toEntityWithoutId(HistorianDTO dto) {
        return new Historian(
                dto.getName(),
                dto.getPrefix(),
                dto.getProvider(),
                dto.getDataSource(),
                dto.getUserId(),
                dto.getPassword(),
                dto.getVersion());
    }
}
