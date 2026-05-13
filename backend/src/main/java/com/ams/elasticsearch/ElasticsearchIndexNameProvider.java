package com.ams.elasticsearch;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component("elasticsearchIndexNameProvider")
public class ElasticsearchIndexNameProvider {

    private static final String INDEX_PREFIX = "ams-logs-";

    public String getIndexName() {
        return INDEX_PREFIX + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getIndexName(LocalDate date) {
        return INDEX_PREFIX + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
