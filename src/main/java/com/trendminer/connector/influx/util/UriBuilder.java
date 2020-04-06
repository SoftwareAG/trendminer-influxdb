package com.trendminer.connector.influx.util;

import com.trendminer.connector.database.Historian;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UriBuilder {

    public UriComponentsBuilder fromPath(Historian historian, String path) {
        return UriComponentsBuilder
                .fromUriString(historian.getDataSource())
                .pathSegment(path);
    }
    public UriComponentsBuilder fromQuery(Historian historian, String query) {
        return UriComponentsBuilder
                .fromUriString(historian.getDataSource())
//                .queryParam("q", query + historian.getName());  //historian name is db name
                .queryParam("q", query)  //historian name is db name
                .queryParam("db",   historian.getName());
    }
}
