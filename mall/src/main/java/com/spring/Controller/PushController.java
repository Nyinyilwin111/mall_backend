package com.spring.Controller;

import com.spring.DTO.request.GetPushMessageDto;
import com.spring.DTO.request.SubscriptionDto;
import com.spring.Entity.*;
import com.spring.Entity.PushMessage;
import com.spring.RepositoryMain.*;
import com.spring.Services.PushMessageService;
import com.spring.Services.UserService;
import com.spring.SubscriptionMap.SubscriptionMapper;
import nl.martijndwars.webpush.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/push")
@CrossOrigin(origins = "*")
public class PushController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PushMessageService pushMessageService;

    @Autowired
    private WebSocketNotificationController webSocketNotificationController;

    @Value("${vapid.public}")
    private String publicKey;

    @Value("${vapid.private}")
    private String privateKey;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // Enhanced subscribe method with WebSocket notification
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody SubscriptionDto dto,
                                                         @RequestParam String userId) {
        System.out.println("reach in subscribtion controller !.........");
        Map<String, Object> response = new HashMap<>();
        try {
            UUID uuid = UUID.fromString(userId);
            Optional<User> userOpt = userRepository.findById(uuid);

            if (userOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            SubscriptionEntity newEntity = SubscriptionMapper.toEntity(dto);

            // OPTION 1: Clear ALL existing subscriptions for this user
            System.out.println("🗑️ Clearing " + user.getSubscriptions().size() + " existing subscriptions for user: " + user.getEmail());
            user.getSubscriptions().clear();

            // Add the new subscription
            user.getSubscriptions().add(newEntity);
            userRepository.save(user);

            System.out.println("✅ New subscription added for user: " + user.getEmail() +
                    " (Total: " + user.getSubscriptions().size() + ")");

            response.put("status", "success");
            response.put("message", "Subscription processed successfully");
            response.put("action", "added");
            response.put("subscriptionCount", user.getSubscriptions().size());
            response.put("userEmail", user.getEmail());
            response.put("clearedPrevious", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error processing subscription: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Enhanced sendAll with WebSocket notifications
    @PostMapping("/sendAll")
    public ResponseEntity<Map<String, Object>> sendAll(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title");
        String body = payload.get("body");
        String userId = payload.get("sentuserId");

        if (title == null || body == null) {
            response.put("status", "error");
            response.put("message", "Missing title or body");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Save push message
            PushMessage message = new PushMessage();
            message.setMessage(title + " - " + body);
            message.setDateTime(LocalDateTime.now());
            message.setSentToAll(true);

            if (userId != null && !userId.isEmpty()) {
                try {
                    UUID uid = UUID.fromString(userId);
                    userRepository.findById(uid).ifPresent(message::setCreatedUserId);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid user ID format: " + userId);
                }
            }

            PushMessage savedMessage = pushMessageService.save(message);

            // Send push notifications
            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            String jsonBody = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                    title.replace("\"", "\\\""),
                    body.replace("\"", "\\\""),
                    LocalDateTime.now().toString()
            );

            int sentCount = 0;
            int totalSubscriptions = 0;
            Set<UUID> notifiedUsers = new HashSet<>();

            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                totalSubscriptions += user.getSubscriptions().size();
                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    try {
                        nl.martijndwars.webpush.Notification notification = new nl.martijndwars.webpush.Notification(
                                sub.getEndpoint(),
                                sub.getP256dh(),
                                sub.getAuth(),
                                jsonBody.getBytes("UTF-8")
                        );
                        pushService.send(notification);
                        sentCount++;
                        notifiedUsers.add(user.getId());
                        System.out.println("✅ Push sent to: " + user.getEmail());
                    } catch (Exception e) {
                        System.err.println("❌ Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                        // Remove invalid subscriptions
                        if (e.getMessage().contains("410") || e.getMessage().contains("404")) {
                            user.getSubscriptions().removeIf(s -> s.getEndpoint().equals(sub.getEndpoint()));
                            userRepository.save(user);
                            System.out.println("🗑️ Removed invalid subscription for: " + user.getEmail());
                        }
                    }
                }

                // Send WebSocket notification count update
                webSocketNotificationController.sendNotificationCountUpdate(user.getId());
            }

            response.put("status", "success");
            response.put("sent", sentCount);
            response.put("total", totalSubscriptions);
            response.put("notifiedUsers", notifiedUsers.size());
            response.put("messageId", savedMessage.getId() != null ? savedMessage.getId().toString() : null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Enhanced sendBranch with WebSocket
    @PostMapping("/sendBranch")
    public ResponseEntity<Map<String, Object>> sendBranch(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title");
        String body = payload.get("body");
        String branchIdStr = payload.get("branchId");
        String userIdStr = payload.get("sentuserId");

        if (title == null || body == null || branchIdStr == null) {
            response.put("status", "error");
            response.put("message", "Missing title, body, or branchId");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            UUID branchId = UUID.fromString(branchIdStr);
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Branch not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<User> branchUsers = userRepository.findUsersByBranch(branchOpt.get());
            if (branchUsers.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "No users found in this branch");
                return ResponseEntity.ok(response);
            }

            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            int sentCount = 0;
            int totalSubscriptions = 0;

            String jsonBody = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                    title.replace("\"", "\\\""),
                    body.replace("\"", "\\\""),
                    LocalDateTime.now().toString()
            );

            for (User user : branchUsers) {
                // Save message for each user
                PushMessage message = new PushMessage();
                message.setMessage(title + " - " + body);
                message.setDateTime(LocalDateTime.now());
                message.setSentToAll(false);
                message.setBranch(branchOpt.get());
                message.setRecipientUser(user);

                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        UUID senderId = UUID.fromString(userIdStr);
                        userRepository.findById(senderId).ifPresent(message::setCreatedUserId);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid sender ID format: " + userIdStr);
                    }
                }

                PushMessage savedMessage = pushMessageService.save(message);

                // Send web push notification
                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    totalSubscriptions++;
                    try {
                        nl.martijndwars.webpush.Notification notification = new nl.martijndwars.webpush.Notification(
                                sub.getEndpoint(),
                                sub.getP256dh(),
                                sub.getAuth(),
                                jsonBody.getBytes("UTF-8")
                        );
                        pushService.send(notification);
                        sentCount++;
                        System.out.println("✅ Push sent to branch user: " + user.getEmail());
                    } catch (Exception e) {
                        System.err.println("❌ Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                    }
                }

                // Send WebSocket notification
                webSocketNotificationController.sendNotificationCountUpdate(user.getId());
                webSocketNotificationController.notifyNewMessage(savedMessage, user.getId());
            }

            response.put("status", "success");
            response.put("sent", sentCount);
            response.put("total", totalSubscriptions);
            response.put("branchUsers", branchUsers.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Enhanced sendToUser with WebSocket
    @PostMapping("/sendToUser")
    public ResponseEntity<Map<String, Object>> sendToUser(@RequestBody Map<String, String> payload) {
        System.out.println("🎯 Reached sendToUser endpoint");

        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title");
        String body = payload.get("body");
        String userIdStr = payload.get("userId");
        String senderIdStr = payload.get("sentuserId");

        if (title == null || body == null || userIdStr == null) {
            response.put("status", "error");
            response.put("message", "Missing title, body, or userId");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            UUID userId = UUID.fromString(userIdStr);
            Optional<User> userOpt = userRepository.findById(userId);
            System.out.println("👤 Target user found: " + userOpt.isPresent());

            if (userOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();

            // Save push message even if user has no subscriptions (for WebSocket)
            PushMessage message = new PushMessage();
            message.setMessage(title + " - " + body);
            message.setDateTime(LocalDateTime.now());
            message.setSentToAll(false);
            message.setRecipientUser(user);

            if (senderIdStr != null && !senderIdStr.isEmpty()) {
                try {
                    UUID senderId = UUID.fromString(senderIdStr);
                    userRepository.findById(senderId).ifPresent(message::setCreatedUserId);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid sender ID format: " + senderIdStr);
                }
            }

            PushMessage savedMessage = pushMessageService.save(message);
            System.out.println("💾 Message saved with ID: " + savedMessage.getId());

            // Send push notification only if user has subscriptions
            int sentCount = 0;
            if (!user.getSubscriptions().isEmpty()) {
                PushService pushService = new PushService();
                pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
                pushService.setPublicKey(Utils.loadPublicKey(publicKey));
                pushService.setSubject("mailto:admin@seingahar.com");

                String jsonBody = String.format(
                        "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                        title.replace("\"", "\\\""),
                        body.replace("\"", "\\\""),
                        LocalDateTime.now().toString()
                );

                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    try {
                        nl.martijndwars.webpush.Notification notification = new nl.martijndwars.webpush.Notification(
                                sub.getEndpoint(),
                                sub.getP256dh(),
                                sub.getAuth(),
                                jsonBody.getBytes("UTF-8")
                        );
                        pushService.send(notification);
                        sentCount++;
                        System.out.println("✅ Push sent to user: " + user.getEmail());
                    } catch (Exception e) {
                        System.err.println("❌ Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                    }
                }
            } else {
                System.out.println("ℹ️ User has no active subscriptions, only saving message for WebSocket");
            }

            // Send WebSocket notifications
            webSocketNotificationController.sendNotificationCountUpdate(user.getId());
            webSocketNotificationController.notifyNewMessage(savedMessage, user.getId());

            response.put("status", "success");
            response.put("sent", sentCount);
            response.put("hasSubscriptions", !user.getSubscriptions().isEmpty());
            response.put("messageId", savedMessage.getId() != null ? savedMessage.getId().toString() : null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Enhanced markAsRead with WebSocket
    @PutMapping("/message/{messageId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String messageId) {
        Map<String, Object> response = new HashMap<>();
        try {
            UUID messageUuid = UUID.fromString(messageId);
            PushMessage message = pushMessageService.markAsRead(messageUuid);

            if (message != null && message.getRecipientUser() != null) {
                // Send WebSocket count update
                webSocketNotificationController.sendNotificationCountUpdate(message.getRecipientUser().getId());
            }

            response.put("status", "success");
            response.put("message", "Message marked as read");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get unread count endpoint
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            UUID userUuid = UUID.fromString(userId);
            Long unreadCount = pushMessageService.getUnreadCount(userUuid);

            response.put("status", "success");
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Existing methods remain the same...
    @GetMapping("/vapidPublicKey")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", publicKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetPushMessageDto>> getUserMessages(@PathVariable String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            List<GetPushMessageDto> messages = pushMessageService.getMessagesForUser(userUuid);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestParam String userId,
                                                           @RequestParam String endpoint) {
        Map<String, Object> response = new HashMap<>();
        try {
            UUID uuid = UUID.fromString(userId);
            Optional<User> userOpt = userRepository.findById(uuid);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean removed = user.getSubscriptions().removeIf(
                        sub -> sub.getEndpoint().equals(endpoint)
                );

                if (removed) {
                    userRepository.save(user);
                    response.put("status", "success");
                    response.put("message", "Unsubscribed successfully");
                    return ResponseEntity.ok(response);
                }
            }

            response.put("status", "error");
            response.put("message", "Subscription not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error unsubscribing: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}