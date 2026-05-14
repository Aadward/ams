package com.ams.dto;

import com.ams.enums.ConsumableRecordType;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableRecordResponse {
    private Long id;
    private Long consumableId;
    private String consumableName;
    private ConsumableRecordType type;
    private String typeLabel;
    private Integer quantity;
    private Long relatedUserId;
    private String relatedUserName;
    private String supplier;
    private String remark;
    private LocalDateTime createdAt;
}
