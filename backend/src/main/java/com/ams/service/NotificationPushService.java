package com.ams.service;

import com.ams.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Pushes real-time notifications to connected WebSocket clients.
 * Clients subscribe to /topic/notifications/{userId} to receive live updates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPushService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push a notification to the specified user via WebSocket.
     *
     * @param userId       the target user ID
     * @param notification the notification payload
     */
    public void pushToUser(Long userId, Notification notification) {
        String destination = "/topic/notifications/" + userId;
        log.debug("Pushing notification {} to WebSocket destination {}", notification.getId(), destination);
        messagingTemplate.convertAndSend(destination, notification);
    }
}
