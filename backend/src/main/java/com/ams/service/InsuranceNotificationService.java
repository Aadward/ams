package com.ams.service;

import com.ams.entity.Asset;
import com.ams.entity.InsurancePolicy;
import com.ams.enums.InsuranceStatus;
import com.ams.enums.NotificationType;
import com.ams.repository.InsurancePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceNotificationService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final NotificationService notificationService;

    private static final int DEFAULT_DAYS = 30;
    private static final Set<Long> notifiedPolicyIds = new HashSet<>();

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(readOnly = true)
    public void checkExpiringInsurances() {
        log.info("Running insurance expiration check...");
        int days = DEFAULT_DAYS;
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<InsurancePolicy> expiringPolicies = insurancePolicyRepository.findByExpiringBetween(today, endDate, InsuranceStatus.ACTIVE);
        log.info("Found {} insurance policies expiring within {} days", expiringPolicies.size(), days);

        int notifiedCount = 0;
        for (InsurancePolicy policy : expiringPolicies) {
            if (notifiedPolicyIds.contains(policy.getId())) {
                continue;
            }
            if (policy.getStatus() != InsuranceStatus.ACTIVE) {
                continue;
            }

            Asset asset = policy.getAsset();
            String title = "资产保险即将到期";
            String message = String.format("资产「%s」（编码：%s）的保险（保单号：%s）将于 %s 到期，请及时处理。",
                    asset.getName(),
                    asset.getAssetCode(),
                    policy.getPolicyNumber(),
                    policy.getEndDate());

            Long targetUserId = (asset.getAssignee() != null)
                    ? asset.getAssignee().getId()
                    : 1L;

            notificationService.createNotification(targetUserId, title, message, NotificationType.ASSET_EXPIRING_INSURANCE);
            notifiedPolicyIds.add(policy.getId());
            notifiedCount++;
        }
        log.info("Insurance expiration check completed. {} notifications sent.", notifiedCount);
    }

    public int sendExpiringInsuranceNotifications(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        List<InsurancePolicy> expiringPolicies = insurancePolicyRepository.findByExpiringBetween(today, endDate, InsuranceStatus.ACTIVE);
        int count = 0;
        for (InsurancePolicy policy : expiringPolicies) {
            if (notifiedPolicyIds.contains(policy.getId())) {
                continue;
            }
            Asset asset = policy.getAsset();
            String title = "资产保险即将到期";
            String message = String.format("资产「%s」（编码：%s）的保险（保单号：%s）将于 %s 到期，请及时处理。",
                    asset.getName(),
                    asset.getAssetCode(),
                    policy.getPolicyNumber(),
                    policy.getEndDate());
            if (asset.getAssignee() != null) {
                notificationService.createNotification(
                        asset.getAssignee().getId(), title, message, NotificationType.ASSET_EXPIRING_INSURANCE);
            }
            notifiedPolicyIds.add(policy.getId());
            count++;
        }
        return count;
    }

    public List<InsurancePolicy> getExpiringInsurancePolicies(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return insurancePolicyRepository.findByExpiringBetween(today, endDate, InsuranceStatus.ACTIVE);
    }

    public void clearNotificationHistory() {
        notifiedPolicyIds.clear();
    }
}
