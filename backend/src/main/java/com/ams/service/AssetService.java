package com.ams.service;

import com.ams.dto.AssetCreateRequest;
import com.ams.dto.AssetResponse;
import com.ams.dto.AssetUpdateRequest;
import com.ams.entity.Asset;
import com.ams.entity.AssetLog;
import com.ams.entity.Employee;
import com.ams.enums.AssetAction;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.enums.NotificationType;
import com.ams.repository.AssetLogRepository;
import com.ams.repository.AssetRepository;
import com.ams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetLogRepository assetLogRepository;
    private final ElasticsearchLogService elasticsearchLogService;
    private final NotificationService notificationService;

    private static final String OPERATOR = "system";

    @Transactional
    public AssetResponse createAsset(AssetCreateRequest request) {
        Asset asset = Asset.builder()
                .assetCode(request.getAssetCode())
                .name(request.getName())
                .category(AssetCategory.valueOf(request.getCategory()))
                .status(AssetStatus.IN_STOCK)
                .spec(request.getSpec())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .warrantyEnd(request.getWarrantyEnd())
                .supplier(request.getSupplier())
                .location(request.getLocation())
                .deleted(false)
                .build();
        asset = assetRepository.save(asset);

        saveLog(asset, AssetAction.CREATE, "创建资产");

        return toResponse(asset);
    }

    @Transactional
    public AssetResponse updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        if (request.getName() != null) {
            asset.setName(request.getName());
        }
        if (request.getCategory() != null) {
            asset.setCategory(AssetCategory.valueOf(request.getCategory()));
        }
        if (request.getSpec() != null) {
            asset.setSpec(request.getSpec());
        }
        if (request.getPurchaseDate() != null) {
            asset.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getPurchasePrice() != null) {
            asset.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getWarrantyEnd() != null) {
            asset.setWarrantyEnd(request.getWarrantyEnd());
        }
        if (request.getSupplier() != null) {
            asset.setSupplier(request.getSupplier());
        }
        if (request.getLocation() != null) {
            asset.setLocation(request.getLocation());
        }
        asset = assetRepository.save(asset);

        saveLog(asset, AssetAction.UPDATE, "更新资产");

        return toResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponse getAsset(Long id) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        return toResponse(asset);
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        asset.setDeleted(true);
        assetRepository.save(asset);

        saveLog(asset, AssetAction.RESTORE, "删除资产");
    }

    @Transactional
    public AssetResponse assignAsset(Long id, Long assigneeId) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        Employee employee = employeeRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        asset.setAssignee(employee);
        asset.setStatus(AssetStatus.IN_USE);
        asset = assetRepository.save(asset);

        saveLog(asset, AssetAction.ASSIGN, "领用人: " + employee.getName());

        notificationService.createNotification(
                employee.getId(),
                "资产已领用",
                "您已领用资产：「" + asset.getName() + "」（编号：" + asset.getAssetCode() + "）",
                NotificationType.ASSET_ASSIGNED
        );

        return toResponse(asset);
    }

    @Transactional
    public AssetResponse unassignAsset(Long id) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        String assigneeName = asset.getAssignee() != null ? asset.getAssignee().getName() : "无";
        asset.setAssignee(null);
        asset.setStatus(AssetStatus.IN_STOCK);
        asset = assetRepository.save(asset);

        saveLog(asset, AssetAction.UNASSIGN, "归还,原领用人: " + assigneeName);

        notificationService.createNotification(
                asset.getAssignee().getId(),
                "资产已归还",
                "资产「" + asset.getName() + "」（编号：" + asset.getAssetCode() + "）已归还库存",
                NotificationType.ASSET_RETURNED
        );

        return toResponse(asset);
    }

    @Transactional
    public AssetResponse retireAsset(Long id) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        asset.setStatus(AssetStatus.RETIRED);
        asset.setAssignee(null);
        asset = assetRepository.save(asset);

        saveLog(asset, AssetAction.RETIRE, "报废资产");

        return toResponse(asset);
    }

    // ========== Batch Operations ==========

    @Transactional
    public int batchAssign(List<Long> assetIds, Long assigneeId) {
        Employee employee = employeeRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        int count = 0;
        for (Long assetId : assetIds) {
            try {
                Asset asset = assetRepository.findByIdAndDeletedFalse(assetId).orElse(null);
                if (asset == null) continue;
                asset.setAssignee(employee);
                asset.setStatus(AssetStatus.IN_USE);
                assetRepository.save(asset);
                saveLog(asset, AssetAction.ASSIGN, "批量领用,领用人: " + employee.getName());
                count++;
            } catch (Exception e) {
                log.warn("批量操作失败 assetId={}: {}", assetId, e.getMessage());
            }
        }
        return count;
    }

    @Transactional
    public int batchUnassign(List<Long> assetIds) {
        int count = 0;
        for (Long assetId : assetIds) {
            try {
                Asset asset = assetRepository.findByIdAndDeletedFalse(assetId).orElse(null);
                if (asset == null) continue;
                String assigneeName = asset.getAssignee() != null ? asset.getAssignee().getName() : "无";
                asset.setAssignee(null);
                asset.setStatus(AssetStatus.IN_STOCK);
                assetRepository.save(asset);
                saveLog(asset, AssetAction.UNASSIGN, "批量归还,原领用人: " + assigneeName);
                count++;
            } catch (Exception e) {
                log.warn("批量操作失败 assetId={}: {}", assetId, e.getMessage());
            }
        }
        return count;
    }

    @Transactional
    public int batchRetire(List<Long> assetIds) {
        int count = 0;
        for (Long assetId : assetIds) {
            try {
                Asset asset = assetRepository.findByIdAndDeletedFalse(assetId).orElse(null);
                if (asset == null) continue;
                asset.setStatus(AssetStatus.RETIRED);
                asset.setAssignee(null);
                assetRepository.save(asset);
                saveLog(asset, AssetAction.RETIRE, "批量报废");
                count++;
            } catch (Exception e) {
                log.warn("批量操作失败 assetId={}: {}", assetId, e.getMessage());
            }
        }
        return count;
    }

    @Transactional
    public int batchUpdateLocation(List<Long> assetIds, String location) {
        int count = 0;
        for (Long assetId : assetIds) {
            try {
                Asset asset = assetRepository.findByIdAndDeletedFalse(assetId).orElse(null);
                if (asset == null) continue;
                asset.setLocation(location);
                assetRepository.save(asset);
                saveLog(asset, AssetAction.UPDATE, "批量更新位置: " + location);
                count++;
            } catch (Exception e) {
                log.warn("批量操作失败 assetId={}: {}", assetId, e.getMessage());
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public Page<AssetResponse> searchAssets(String category, String status, String keyword, Pageable pageable) {
        AssetCategory assetCategory = null;
        AssetStatus assetStatus = null;

        if (category != null && !category.isEmpty()) {
            assetCategory = AssetCategory.valueOf(category);
        }
        if (status != null && !status.isEmpty()) {
            assetStatus = AssetStatus.valueOf(status);
        }

        Page<Asset> assets = assetRepository.searchAssets(assetCategory, assetStatus, keyword, pageable);
        return assets.map(this::toResponse);
    }

    private AssetResponse toResponse(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .assetCode(asset.getAssetCode())
                .name(asset.getName())
                .category(asset.getCategory().name())
                .status(asset.getStatus().name())
                .spec(asset.getSpec())
                .purchaseDate(asset.getPurchaseDate())
                .purchasePrice(asset.getPurchasePrice())
                .warrantyEnd(asset.getWarrantyEnd())
                .supplier(asset.getSupplier())
                .location(asset.getLocation())
                .assigneeId(asset.getAssignee() != null ? asset.getAssignee().getId() : null)
                .assigneeName(asset.getAssignee() != null ? asset.getAssignee().getName() : null)
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }

    private void saveLog(Asset asset, AssetAction action, String detail) {
        AssetLog log = AssetLog.builder()
                .asset(asset)
                .action(action)
                .operator(OPERATOR)
                .detail(detail)
                .build();
        assetLogRepository.save(log);
        elasticsearchLogService.saveLog(log);
    }
}