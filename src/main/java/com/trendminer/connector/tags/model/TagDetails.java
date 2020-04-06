package com.trendminer.connector.tags.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "historian"})})
public class TagDetails {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private int historian;

    @Enumerated(EnumType.STRING)
    private TagType tagType;

    public TagDetails() {
    }

    public TagDetails(String name, int historian, TagType tagType) {
        this.name = name;
        this.historian = historian;
        this.tagType = tagType;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHistorian() {
        return historian;
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHistorian(int historian) {
        this.historian = historian;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TagDetails)) {
            return false;
        }

        TagDetails that = (TagDetails) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(historian, that.historian)
                .append(name, that.name)
                .append(tagType, that.tagType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(historian)
                .append(tagType)
                .toHashCode();
    }
}
