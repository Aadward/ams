package com.ams.service;

import com.ams.elasticsearch.AssetLogDocument;
import com.ams.elasticsearch.ElasticsearchIndexNameProvider;
import com.ams.entity.AssetLog;
import com.ams.repository.AssetLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchLogService {

    private final AssetLogRepository assetLogRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchIndexNameProvider elasticsearchIndexNameProvider;

    public void saveLog(AssetLog log) {
        try {
            AssetLogDocument doc = AssetLogDocument.builder()
                    .id(log.getId())
                    .assetId(log.getAsset() != null ? log.getAsset().getId() : null)
                    .assetCode(log.getAsset() != null ? log.getAsset().getAssetCode() : null)
                    .action(log.getAction().name())
                    .operator(log.getOperator())
                    .detail(log.getDetail())
                    .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().toInstant(java.time.ZoneOffset.UTC) : null)
                    .timestamp(java.time.Instant.now())
                    .build();
            elasticsearchOperations.save(doc, elasticsearchIndexNameProvider.getIndexName());
        } catch (Exception e) {
        }
    }
}