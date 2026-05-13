package com.ams.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ams.elasticsearch.AssetLogDocument;
import com.ams.elasticsearch.ElasticsearchIndexNameProvider;
import com.ams.entity.AssetLog;
import com.ams.repository.AssetLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Service
@Slf4j
public class ElasticsearchLogService {

    private final AssetLogRepository assetLogRepository;
    private final ElasticsearchIndexNameProvider elasticsearchIndexNameProvider;
    private final String esUri;

    private RestClient restClient;
    private ElasticsearchTransport transport;
    private ElasticsearchClient client;

    public ElasticsearchLogService(
            AssetLogRepository assetLogRepository,
            ElasticsearchIndexNameProvider elasticsearchIndexNameProvider,
            @Value("${spring.elasticsearch.uris:http://localhost:9200}") String esUri) {
        this.assetLogRepository = assetLogRepository;
        this.elasticsearchIndexNameProvider = elasticsearchIndexNameProvider;
        this.esUri = esUri;
    }

    @PostConstruct
    public void init() {
        try {
            HttpHost host = HttpHost.create(esUri);
            this.restClient = RestClient.builder(host).build();
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            this.transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
            this.client = new ElasticsearchClient(transport);
            log.info("ElasticsearchRestClient initialized, connected to {}", esUri);
        } catch (Exception e) {
            log.error("Failed to initialize ElasticsearchRestClient: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (transport != null) transport.close();
            if (restClient != null) restClient.close();
        } catch (IOException e) {
            log.error("Error closing Elasticsearch client: {}", e.getMessage());
        }
    }

    public void saveLog(AssetLog assetLog) {
        if (client == null) {
            log.warn("ElasticsearchClient not available, skipping ES save for log id={}", assetLog.getId());
            return;
        }
        try {
            AssetLogDocument doc = AssetLogDocument.builder()
                    .id(assetLog.getId())
                    .assetId(assetLog.getAsset() != null ? assetLog.getAsset().getId() : null)
                    .assetCode(assetLog.getAsset() != null ? assetLog.getAsset().getAssetCode() : null)
                    .action(assetLog.getAction().name())
                    .operator(assetLog.getOperator())
                    .detail(assetLog.getDetail())
                    .createdAt(assetLog.getCreatedAt() != null ? assetLog.getCreatedAt().toInstant(java.time.ZoneOffset.UTC) : null)
                    .timestamp(java.time.Instant.now())
                    .build();

            String indexName = elasticsearchIndexNameProvider.getIndexName();
            IndexRequest<AssetLogDocument> request = IndexRequest.of(i -> i
                    .index(indexName)
                    .id(String.valueOf(doc.getId()))
                    .document(doc)
            );

            client.index(request);
            log.debug("Saved asset log to ES index={}, id={}", indexName, assetLog.getId());
        } catch (Exception e) {
            log.error("Failed to save asset log to ES: id={}, error={}", assetLog.getId(), e.getMessage(), e);
        }
    }
}
