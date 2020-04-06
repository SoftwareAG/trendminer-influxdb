package com.trendminer.connector.influx.model;

import com.trendminer.connector.database.Historian;
import com.trendminer.connector.tags.model.TagType;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Metric {
    public String getMeasurement() {
        return measurement;
    }

    private String measurement;

    private List<InfluxTag> tags;

    private InfluxField field;

    public Historian getHistorian() {
        return historian;
    }

    private Historian historian;

    public static Metric createMetricPartial(String unparsed, Historian historian) {
        Metric that = new Metric();
        String[] tokens = unparsed.split(",");
        that.setTags(new ArrayList<InfluxTag>());
        for (int i = 0; i < tokens.length; i++) {
            if (i == 0)
                that.measurement = tokens[0];
            else {
                String[] tag = tokens[i].split("=");
                if (tag.length == 2)
                    that.getTags().add (new InfluxTag(tag[0], tag[1]));
            }
        }
        that.historian = historian;
        return that;
    }

    public static Metric createMetricComplete(Historian historian, String unparsed, TagType tagType) {
        Metric that = new Metric();
        String[] tokens = unparsed.split(",");
        that.setTags(new ArrayList<InfluxTag>());
        for (int i = 0; i < tokens.length; i++) {
            if (i == 0)
                that.measurement = tokens[0];
            else if ( i == tokens.length -1) {
                that.setField(new InfluxField(tokens[i], tagType));
            } else  {
                String[] tag = tokens[i].split("=");
                if (tag.length == 2)
                    that.getTags().add (new InfluxTag(tag[0], tag[1]));
            }
        }
        that.historian = historian;
        return that;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String getName(){
        return measurement + "," + String.join("", getTags().stream().map(tag -> tag.getName() + "=" + tag.getValue() + ",").collect(Collectors.toList()));
    }

    public InfluxField getField() {
        return field;
    }

    public void setField(InfluxField field) {
        this.field = field;
    }

    public List<InfluxTag> getTags() {
        return tags;
    }

    public void setTags(List<InfluxTag> tags) {
        this.tags = tags;
    }

    public static class InfluxTag {
        public InfluxTag(String name, String value) {
            this.setName(name);
            this.setValue(value);
        }

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class InfluxField {
        public InfluxField(String name, TagType type) {
            this.setName(name);
            this.setType(type);
        }

        private String name;
        private TagType type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TagType getType() {
            return type;
        }

        public void setType(TagType type) {
            this.type = type;
        }
    }
}
