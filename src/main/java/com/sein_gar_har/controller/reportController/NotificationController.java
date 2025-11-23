package com.sein_gar_har.controller.reportController;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Broadcast to all users
    @MessageMapping("/notifications")
    @SendTo("/topic/notifications")
    public Map<String, Object> handleNotification(Map<String, Object> notification) {
        System.out.println("📨 Received notification via WebSocket: " + notification);

        // Add timestamp if not present
        if (!notification.containsKey("timestamp")) {
            notification.put("timestamp", java.time.Instant.now().toString());
        }

        return notification;
    }

    // Send to specific user
    @MessageMapping("/notifications/{userId}")
    @SendTo("/topic/notifications/{userId}")
    public Map<String, Object> handleUserNotification(
            @org.springframework.messaging.handler.annotation.DestinationVariable String userId,
            Map<String, Object> notification) {
        System.out.println("📨 Received user notification for " + userId + ": " + notification);

        if (!notification.containsKey("timestamp")) {
            notification.put("timestamp", java.time.Instant.now().toString());
        }

        return notification;
    }

    // Alternative method using SimpMessagingTemplate
    public void sendNotificationToAll(Map<String, Object> notification) {
        System.out.println("📤 Broadcasting notification to all users: " + notification);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    public void sendNotificationToUser(String userId, Map<String, Object> notification) {
        System.out.println("📤 Sending notification to user " + userId + ": " + notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}