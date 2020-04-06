package com.trendminer.connector.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HistorianDTO {
    @JsonProperty("Name")
    private final String name;
    @JsonProperty("Prefix")
    private final String prefix;
    @JsonProperty("Provider")
    private final String provider;
    @JsonProperty("DataSource")
    private final String dataSource;
    @JsonProperty("UserId")
    private final String userId;
    @JsonProperty("Password")
    private final String password;
    @JsonProperty("Version")
    private final String version;
    @JsonProperty("DbId")
    private int id;

    @JsonCreator
    public HistorianDTO(
            @JsonProperty("Name") String name,
            @JsonProperty("Prefix") String prefix,
            @JsonProperty("Provider") String provider,
            @JsonProperty("DataSource") String dataSource,
            @JsonProperty("UserId") String userId,
            @JsonProperty("Password") String password,
            @JsonProperty("Version") String version) {
        this.name = name;
        this.prefix = prefix;
        this.provider = provider;
        this.dataSource = dataSource;
        this.userId = userId;
        this.password = password;
        this.version = version;
    }

    @JsonCreator
    public HistorianDTO(
            @JsonProperty("DbId") int id,
            @JsonProperty("Name") String name,
            @JsonProperty("Prefix") String prefix,
            @JsonProperty("Provider") String provider,
            @JsonProperty("DataSource") String dataSource,
            @JsonProperty("UserId") String userId,
            @JsonProperty("Password") String password,
            @JsonProperty("Version") String version) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.provider = provider;
        this.dataSource = dataSource;
        this.userId = userId;
        this.password = password;
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getProvider() {
        return provider;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getVersion() {
        return version;
    }
}
