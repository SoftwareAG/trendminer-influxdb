package com.trendminer.connector.tags.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class TagDetailsDTO {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Historian")
    private int historian;

    @JsonProperty("Units")
    private String units;

    @JsonProperty("Type")
    private TagType type;

    public TagDetailsDTO(String name, String description, int historian, String units, TagType type) {
        this.name = name;
        this.description = description;
        this.historian = historian;
        this.units = units;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getHistorian() {
        return historian;
    }

    public String getUnits() {
        return units;
    }

    public TagType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHistorian(int historian) {
        this.historian = historian;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TagDetailsDTO)) {
            return false;
        }

        TagDetailsDTO that = (TagDetailsDTO) o;

        return new EqualsBuilder()
                .append(historian, that.historian)
                .append(name, that.name)
                .append(description, that.description)
                .append(units, that.units)
                .append(type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(description)
                .append(historian)
                .append(units)
                .append(type)
                .toHashCode();
    }
}
