package com.ams.dto;

import com.ams.enums.ApprovalStatus;
import com.ams.enums.ApprovalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private Long departmentId;
    private String departmentName;
    private ApprovalType type;
    private ApprovalStatus status;
    private String reason;
    private String managerComment;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
