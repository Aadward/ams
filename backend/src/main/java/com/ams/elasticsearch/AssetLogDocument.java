package com.ams.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.Instant;

@Document(indexName = "#{@elasticsearchIndexNameProvider.getIndexName()}")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetLogDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long assetId;

    @Field(type = FieldType.Keyword)
    private String assetCode;

    @Field(type = FieldType.Keyword)
    private String action;

    @Field(type = FieldType.Text)
    private String operator;

    @Field(type = FieldType.Text)
    private String detail;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant timestamp;
}
