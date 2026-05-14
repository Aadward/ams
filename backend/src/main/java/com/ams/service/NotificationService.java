package com.ams.service;

import com.ams.entity.Notification;
import com.ams.enums.NotificationType;
import com.ams.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService notificationPushService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPushService notificationPushService) {
        this.notificationRepository = notificationRepository;
        this.notificationPushService = notificationPushService;
    }

    @Transactional
    public Notification createNotification(Long userId, String title, String message, NotificationType type) {
        log.info("Creating notification for user {} with type {}", userId, type);
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        // Push to WebSocket subscribers in real-time
        notificationPushService.pushToUser(userId, saved);
        return saved;
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {} with pageable", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<Notification> getUnreadNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching unread notifications for user {}", userId);
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Optional<Notification> markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    return notificationRepository.save(notification);
                });
    }

    @Transactional
    public long markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        Page<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, Pageable.unpaged());
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications.getContent());
        return unreadNotifications.getTotalElements();
    }
}
