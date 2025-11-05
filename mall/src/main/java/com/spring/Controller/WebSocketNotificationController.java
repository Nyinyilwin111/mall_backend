package com.spring.Controller;

import com.spring.Entity.Notification;
import com.spring.RepositoryMain.UserRepository;
import com.spring.Services.PushMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class WebSocketNotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PushMessageService pushMessageService;

    @Autowired
    private UserRepository userRepository;

    // Send real-time notification count update
    public void sendNotificationCountUpdate(UUID userId) {
        try {
            Long unreadCount = pushMessageService.getUnreadCount(userId);

            Map<String, Object> countUpdate = new HashMap<>();
            countUpdate.put("type", "COUNT_UPDATE");
            countUpdate.put("unreadCount", unreadCount);
            countUpdate.put("userId", userId.toString());
            countUpdate.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notification-count",
                    countUpdate
            );

            System.out.println("📊 Sent count update to user " + userId + ": " + unreadCount + " unread");

        } catch (Exception e) {
            System.err.println("❌ Error sending count update for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Notify about new message
    public void notifyNewMessage(Notification message, UUID recipientUserId) {
        try {
            Map<String, Object> messageUpdate = new HashMap<>();
            messageUpdate.put("type", "NEW_MESSAGE");
            messageUpdate.put("message", convertToDto(message));
            messageUpdate.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    recipientUserId.toString(),
                    "/queue/new-messages",
                    messageUpdate
            );

            System.out.println("📨 Sent new message notification to user: " + recipientUserId);

        } catch (Exception e) {
            System.err.println("❌ Error sending new message notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Mark message as read via WebSocket
    @MessageMapping("/mark-read")
    public void handleMarkAsRead(String messageId) {
        try {
            UUID messageUuid = UUID.fromString(messageId);
            Notification message = pushMessageService.markAsRead(messageUuid);

            // Get the user who marked it as read to update their count
            if (message != null && message.getRecipientUser() != null) {
                sendNotificationCountUpdate(message.getRecipientUser().getId());
            }

        } catch (Exception e) {
            System.err.println("❌ Error marking message as read via WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Heartbeat endpoint
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(String userId) {
        try {
            // Just acknowledge the heartbeat
            System.out.println("💓 Heartbeat from user: " + userId);

            // Send acknowledgment back
            Map<String, Object> ack = new HashMap<>();
            ack.put("type", "HEARTBEAT_ACK");
            ack.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/heartbeat",
                    ack
            );
        } catch (Exception e) {
            System.err.println("❌ Error handling heartbeat: " + e.getMessage());
        }
    }

    private Map<String, Object> convertToDto(Notification message) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", message.getId() != null ? message.getId().toString() : null);
        dto.put("message", message.getMessage());
        dto.put("dateTime", message.getDateTime());
        dto.put("readby", message.isReadby());
        dto.put("sentToAll", message.isSentToAll());

        if (message.getBranch() != null) {
            dto.put("branchName", message.getBranch().getName());
        }

        if (message.getCreatedUserId() != null) {
            dto.put("senderName", message.getCreatedUserId().getFullName());
        }

        if (message.getRecipientUser() != null) {
            dto.put("recipientUserId", message.getRecipientUser().getId().toString());
        }

        return dto;
    }
}