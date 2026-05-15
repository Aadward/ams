package com.ams.service;

import com.ams.entity.Asset;
import com.ams.enums.NotificationType;
import com.ams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarrantyNotificationService {

    private final AssetRepository assetRepository;
    private final NotificationService notificationService;

    private static final int DEFAULT_DAYS = 30;
    private static final Set<Long> notifiedAssetIds = new HashSet<>();

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void checkExpiringWarranties() {
        log.info("Running warranty expiration check...");
        int days = DEFAULT_DAYS;
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<Asset> expiringAssets = assetRepository.findByDeletedFalseAndWarrantyEndBetween(today, endDate);
        log.info("Found {} assets with warranty expiring within {} days", expiringAssets.size(), days);

        int notifiedCount = 0;
        for (Asset asset : expiringAssets) {
            if (notifiedAssetIds.contains(asset.getId())) {
                continue;
            }

            String title = "资产维保即将到期";
            String message = String.format("资产「%s」（编码：%s）的维保将于 %s 到期，请及时处理。",
                    asset.getName(),
                    asset.getAssetCode(),
                    asset.getWarrantyEnd());

            Long targetUserId = (asset.getAssignee() != null)
                    ? asset.getAssignee().getId()
                    : 1L;

            notificationService.createNotification(targetUserId, title, message, NotificationType.ASSET_EXPIRING_WARRANTY);
            notifiedAssetIds.add(asset.getId());
            notifiedCount++;
        }
        log.info("Warranty check completed. {} notifications sent.", notifiedCount);
    }

    public int sendExpiringWarrantyNotifications(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        List<Asset> expiringAssets = assetRepository.findByDeletedFalseAndWarrantyEndBetween(today, endDate);
        int count = 0;
        for (Asset asset : expiringAssets) {
            if (notifiedAssetIds.contains(asset.getId())) {
                continue;
            }
            String title = "资产维保即将到期";
            String message = String.format("资产「%s」（编码：%s）的维保将于 %s 到期，请及时处理。",
                    asset.getName(), asset.getAssetCode(), asset.getWarrantyEnd());
            if (asset.getAssignee() != null) {
                notificationService.createNotification(
                        asset.getAssignee().getId(), title, message, NotificationType.ASSET_EXPIRING_WARRANTY);
            }
            notifiedAssetIds.add(asset.getId());
            count++;
        }
        return count;
    }

    public List<Asset> getExpiringWarrantyAssets(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days + 1);
        return assetRepository.findByDeletedFalseAndWarrantyEndBetween(today, endDate);
    }
}
