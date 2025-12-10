package com.sein_gar_har.controller;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.PushMessageService;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.dto.request.GetPushMessageDto;
import com.sein_gar_har.dto.request.RoleNotificationRequest;
import com.sein_gar_har.dto.request.SubscriptionDto;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.PushMessage;
import com.sein_gar_har.entity.SubscriptionEntity;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.subscriptionMap.SubscriptionMapper;
import nl.martijndwars.webpush.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    private final SimpMessagingTemplate messagingTemplate;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public PushController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public void sendPush(@RequestParam String message) {
        messagingTemplate.convertAndSend("/topic/push", message);
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
            // Send push notifications
            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            String jsonBody = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                    sanitize(title),
                    sanitize(body),
                    sanitize(LocalDateTime.now().toString())
            );

            int sentCount = 0;
            int totalSubscriptions = 0;
            Set<UUID> notifiedUsers = new HashSet<>();
            List<UUID> messageIds = new ArrayList<>(); // Store all message IDs

            List<User> allUsers = userRepository.findAll();

            // Send global WebSocket notification once
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("title", title);
            wsMessage.put("body", body);
            wsMessage.put("message", title + " - " + body);
            wsMessage.put("dateTime", LocalDateTime.now());
            wsMessage.put("sentToAll", true);
            wsMessage.put("notificationType", "ALL_USERS");
            messagingTemplate.convertAndSend("/topic/notifications", wsMessage);

            for (User user : allUsers) {
                totalSubscriptions += user.getSubscriptions().size();

                // Save individual push message for each user
                PushMessage message = new PushMessage();
                message.setMessage(title + " - " + body);
                message.setDateTime(LocalDateTime.now());
                message.setSentToAll(true);
                message.setRecipientUser(user);

                if (userId != null && !userId.isEmpty()) {
                    try {
                        UUID uid = UUID.fromString(userId);
                        userRepository.findById(uid).ifPresent(message::setCreatedUserId);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid user ID format: " + userId);
                    }
                }

                PushMessage savedMessage = pushMessageService.save(message);
                messageIds.add(savedMessage.getId()); // Store the message ID

                // Send individual WebSocket notification to user
                Map<String, Object> userWsMessage = new HashMap<>();
                userWsMessage.put("id", savedMessage.getId());
                userWsMessage.put("title", title);
                userWsMessage.put("body", body);
                userWsMessage.put("message", title + " - " + body);
                userWsMessage.put("dateTime", savedMessage.getDateTime());
                userWsMessage.put("sentToAll", true);
                userWsMessage.put("notificationType", "ALL_USERS");
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/notifications", userWsMessage);

                // Send push notifications to user's subscriptions
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
            response.put("messageIds", messageIds); // Return all message IDs
            response.put("totalMessagesCreated", messageIds.size());
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
            Long number = Long.parseLong(branchIdStr);
            Optional<Branch> branchOpt = branchRepository.findById(number);
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
                    sanitize(title),
                    sanitize(body),
                    sanitize(LocalDateTime.now().toString())
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

                // Send WebSocket notification to specific user
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("id", savedMessage.getId());
                wsMessage.put("title", title);
                wsMessage.put("body", body);
                wsMessage.put("message", title + " - " + body);
                wsMessage.put("dateTime", savedMessage.getDateTime());
                wsMessage.put("sentToAll", false);
                wsMessage.put("branchName", branchOpt.get().getName());
                wsMessage.put("targetUserId", user.getId());
                wsMessage.put("notificationType", "BRANCH_USERS");

                messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), wsMessage);

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

                // Send WebSocket notification count update
                webSocketNotificationController.sendNotificationCountUpdate(user.getId());
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

            // Send WebSocket notification to specific user
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("id", savedMessage.getId());
            wsMessage.put("title", title);
            wsMessage.put("body", body);
            wsMessage.put("message", title + " - " + body);
            wsMessage.put("dateTime", savedMessage.getDateTime());
            wsMessage.put("sentToAll", false);
            wsMessage.put("targetUserId", user.getId());
            wsMessage.put("notificationType", "SPECIFIC_USER");

            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), wsMessage);

            // Send push notification only if user has subscriptions
            int sentCount = 0;
            if (!user.getSubscriptions().isEmpty()) {
                PushService pushService = new PushService();
                pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
                pushService.setPublicKey(Utils.loadPublicKey(publicKey));
                pushService.setSubject("mailto:admin@seingahar.com");

                String jsonBody = String.format(
                        "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                        sanitize(title),
                        sanitize(body),
                        sanitize(LocalDateTime.now().toString())
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

    @PostMapping("/sendToRole")
    public ResponseEntity<Map<String, Object>> sendToRole(@RequestBody RoleNotificationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.findByRole(request.getRole());

            if (users.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "No users found with role: " + request.getRole());
                return ResponseEntity.ok(response);
            }

            int sentCount = 0;
            int failedCount = 0;

            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            String jsonBody = String.format(
                    "{" +
                            "\"title\":\"%s\"," +
                            "\"body\":\"%s\"," +
                            "\"role\":\"%s\"," +
                            "\"type\":\"%s\"," +
                            "\"leaseId\":\"%s\"," +
                            "\"spaceId\":\"%s\"," +
                            "\"spaceCode\":\"%s\"," +
                            "\"tenantName\":\"%s\"," +
                            "\"tenantId\":\"%s\"," +
                            "\"branchName\":\"%s\"," +
                            "\"branchID\":\"%s\"," +
                            "\"createdUserName\":\"%s\"," +
                            "\"rentAmount\":\"%s\"," +
                            "\"userToken\":\"%s\"," +
                            "\"url\":\"/lease-management\"," +
                            "\"icon\":\"/sgh.png\"," +
                            "\"badge\":\"/sgh.png\"," +
                            "\"timestamp\":\"%s\"" +
                            "}",
                    sanitize(request.getTitle()),
                    sanitize(request.getBody()),
                    sanitize(request.getRole()),
                    sanitize(request.getType() != null ? request.getType() : "general"),
                    sanitize(request.getLeaseId() != null ? request.getLeaseId() : ""),
                    sanitize(request.getSpaceId() != null ? request.getSpaceId() : ""),
                    sanitize(request.getSpaceCode() != null ? request.getSpaceCode() : ""),
                    sanitize(request.getTenantName() != null ? request.getTenantName() : ""),
                    sanitize(request.getTenantId() != null ? request.getTenantId() : ""),
                    sanitize(request.getBranchName() != null ? request.getBranchName() : ""),
                    sanitize(request.getBranchID() != null ? request.getBranchID() : ""),
                    sanitize(request.getCreatedUserName() != null ? request.getCreatedUserName() : ""),
                    sanitize(request.getRentAmount() != null ? request.getRentAmount() : ""),
                    sanitize(request.getUserToken() != null ? request.getUserToken() : ""),
                    LocalDateTime.now().toString()
            );

            for (User user : users) {
                try {
                    // Save push message for each user
                    PushMessage message = new PushMessage();
                    message.setMessage(request.getTitle() + " - " + request.getBody());
                    message.setDateTime(LocalDateTime.now());
                    message.setSentToAll(false);
                    message.setRecipientUser(user);

                    // 🔴 CRITICAL: Set branch relationship from branchID
                    if (request.getBranchID() != null && !request.getBranchID().isEmpty()) {
                        try {
                            Long branchId = Long.parseLong(request.getBranchID());
                            Optional<Branch> branchOpt = branchRepository.findById(branchId);
                            if (branchOpt.isPresent()) {
                                message.setBranch(branchOpt.get());
                                System.out.println("✅ Set branch relationship for message: " + branchOpt.get().getId());
                            } else {
                                System.err.println("❌ Branch not found with ID: " + request.getBranchID());
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid branch ID format: " + request.getBranchID());
                        }
                    }

                    // Set lease notification fields
                    message.setType(request.getType());
                    message.setTenantId(request.getTenantId());
                    message.setSpaceId(request.getSpaceId());
                    message.setSpaceCode(request.getSpaceCode());
                    message.setTenantName(request.getTenantName());
                    message.setRentAmount(request.getRentAmount());
                    message.setCreatedUserName(request.getCreatedUserName());

                    if (request.getSentuserId() != null && !request.getSentuserId().isEmpty()) {
                        try {
                            UUID senderId = UUID.fromString(request.getSentuserId());
                            userRepository.findById(senderId).ifPresent(message::setCreatedUserId);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid sender ID format: " + request.getSentuserId());
                        }
                    }

                    PushMessage savedMessage = pushMessageService.save(message);
                    System.out.println("💾 Saved message with branch: " +
                            (savedMessage.getBranch() != null ? savedMessage.getBranch().getId() : "null"));

                    // Send WebSocket notification with ALL data
                    Map<String, Object> wsMessage = new HashMap<>();
                    wsMessage.put("id", savedMessage.getId());
                    wsMessage.put("title", request.getTitle());
                    wsMessage.put("body", request.getBody());
                    wsMessage.put("message", request.getTitle() + " - " + request.getBody());
                    wsMessage.put("dateTime", savedMessage.getDateTime());
                    wsMessage.put("sentToAll", false);
                    wsMessage.put("targetUserId", user.getId());
                    wsMessage.put("notificationType", "ROLE_USERS");

                    // Include all lease data
                    wsMessage.put("type", request.getType());
                    wsMessage.put("tenantId", request.getTenantId());
                    wsMessage.put("branchID", request.getBranchID());
                    wsMessage.put("spaceId", request.getSpaceId());
                    wsMessage.put("spaceCode", request.getSpaceCode());
                    wsMessage.put("tenantName", request.getTenantName());
                    wsMessage.put("rentAmount", request.getRentAmount());
                    wsMessage.put("createdUserName", request.getCreatedUserName());
                    wsMessage.put("branchName", request.getBranchName());

                    messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), wsMessage);

                    // Send push notifications
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
                            System.out.println("✅ Push sent to " + request.getRole() + " user: " + user.getEmail());
                            System.out.println(" message sent to manager ");
                        } catch (Exception e) {
                            failedCount++;
                            System.err.println("❌ Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                        }
                    }

                    webSocketNotificationController.sendNotificationCountUpdate(user.getId());

                } catch (Exception e) {
                    failedCount++;
                    System.err.println("❌ Error processing user " + user.getEmail() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            response.put("status", "success");
            response.put("message", String.format("Notification sent to %d %s users", sentCount, request.getRole()));
            response.put("sent", sentCount);
            response.put("failed", failedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Failed to send notification to role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/sendToRoles")
    public ResponseEntity<Map<String, Object>> sendToRoles(@RequestBody RoleNotificationRequest request) {
        System.out.println("🎯 REACHED sendToRoles ENDPOINT");
        System.out.println("📊 Received request: " + request.toString());

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate required fields
            if (request.getTitle() == null || request.getBody() == null || request.getRole() == null) {
                response.put("status", "error");
                response.put("message", "Missing required fields: title, body, or role");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("👥 Looking for users with role: " + request.getRole());
            List<User> users = userService.findByRole(request.getRole());
            System.out.println("👥 Found " + users.size() + " users with role: " + request.getRole());

            if (users.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "No users found with role: " + request.getRole());
                return ResponseEntity.ok(response);
            }

            int sentCount = 0;
            int webSocketCount = 0;
            int pushMessageCount = 0;

            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            // Prepare JSON body with ALL fields
            String jsonBody = String.format(
                    "{" +
                            "\"title\":\"%s\"," +
                            "\"body\":\"%s\"," +
                            "\"role\":\"%s\"," +
                            "\"type\":\"%s\"," +
                            "\"leaseId\":\"%s\"," +
                            "\"spaceId\":\"%s\"," +
                            "\"spaceCode\":\"%s\"," +
                            "\"tenantName\":\"%s\"," +
                            "\"tenantId\":\"%s\"," +
                            "\"branchName\":\"%s\"," +
                            "\"branchID\":\"%s\"," +
                            "\"createdUserName\":\"%s\"," +
                            "\"rentAmount\":\"%s\"," +
                            "\"userToken\":\"%s\"," +
                            "\"url\":\"/lease-management\"," +
                            "\"icon\":\"/sgh.png\"," +
                            "\"badge\":\"/sgh.png\"," +
                            "\"timestamp\":\"%s\"" +
                            "}",
                    sanitize(request.getTitle()),
                    sanitize(request.getBody()),
                    sanitize(request.getRole()),
                    sanitize(request.getType() != null ? request.getType() : "payment"),
                    sanitize(request.getLeaseId() != null ? request.getLeaseId() : ""),
                    sanitize(request.getSpaceId() != null ? request.getSpaceId() : ""),
                    sanitize(request.getSpaceCode() != null ? request.getSpaceCode() : ""),
                    sanitize(request.getTenantName() != null ? request.getTenantName() : ""),
                    sanitize(request.getTenantId() != null ? request.getTenantId() : ""),
                    sanitize(request.getBranchName() != null ? request.getBranchName() : ""),
                    sanitize(request.getBranchID() != null ? request.getBranchID() : ""),
                    sanitize(request.getCreatedUserName() != null ? request.getCreatedUserName() : ""),
                    sanitize(request.getRentAmount() != null ? request.getRentAmount() : ""),
                    sanitize(request.getUserToken() != null ? request.getUserToken() : ""),
                    LocalDateTime.now().toString()
            );

            System.out.println("📤 JSON Body for push notification: " + jsonBody);

            for (User user : users) {
                try {
                    System.out.println("👤 Processing user: " + user.getEmail() + " | Role: " + user.getRoles().toString());

                    // Save push message for each user
                    PushMessage message = new PushMessage();
                    message.setMessage(request.getTitle() + " - " + request.getBody());
                    message.setDateTime(LocalDateTime.now());
                    message.setSentToAll(false);
                    message.setRecipientUser(user);
                    message.setType(request.getType() != null ? request.getType() : "payment");
                    message.setLeaseId(request.getLeaseId());
                    message.setSpaceCode(request.getSpaceCode());
                    message.setTenantName(request.getTenantName());
                    message.setRentAmount(request.getRentAmount());
                    message.setCreatedUserName(request.getCreatedUserName());

                    // Set branch if branchID provided
                    if (request.getBranchID() != null && !request.getBranchID().isEmpty()) {
                        try {
                            Long branchId = Long.parseLong(request.getBranchID());
                            Optional<Branch> branchOpt = branchRepository.findById(branchId);
                            branchOpt.ifPresent(message::setBranch);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid branch ID format: " + request.getBranchID());
                        }
                    }

                    // Set sender if provided
                    if (request.getSentuserId() != null && !request.getSentuserId().isEmpty()) {
                        try {
                            UUID senderId = UUID.fromString(request.getSentuserId());
                            userRepository.findById(senderId).ifPresent(message::setCreatedUserId);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid sender ID format: " + request.getSentuserId());
                        }
                    }

                    PushMessage savedMessage = pushMessageService.save(message);
                    pushMessageCount++;
                    System.out.println("💾 Saved message ID: " + savedMessage.getId());

                    // Send WebSocket notification
                    Map<String, Object> wsMessage = new HashMap<>();
                    wsMessage.put("id", savedMessage.getId());
                    wsMessage.put("title", request.getTitle());
                    wsMessage.put("body", request.getBody());
                    wsMessage.put("message", request.getTitle() + " - " + request.getBody());
                    wsMessage.put("dateTime", savedMessage.getDateTime());
                    wsMessage.put("sentToAll", false);
                    wsMessage.put("targetUserId", user.getId());
                    wsMessage.put("notificationType", "ROLE_USERS");
                    wsMessage.put("type", request.getType());
                    wsMessage.put("leaseId", request.getLeaseId());
                    wsMessage.put("spaceCode", request.getSpaceCode());
                    wsMessage.put("tenantName", request.getTenantName());
                    wsMessage.put("rentAmount", request.getRentAmount());
                    wsMessage.put("createdUserName", request.getCreatedUserName());

                    messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), wsMessage);
                    webSocketCount++;
                    System.out.println("📡 WebSocket sent to user: " + user.getEmail());

                    // Send push notifications to user's subscriptions
                    if (!user.getSubscriptions().isEmpty()) {
                        for (SubscriptionEntity sub : user.getSubscriptions()) {
                            try {
                                nl.martijndwars.webpush.Notification notification =
                                        new nl.martijndwars.webpush.Notification(
                                                sub.getEndpoint(),
                                                sub.getP256dh(),
                                                sub.getAuth(),
                                                jsonBody.getBytes("UTF-8")
                                        );
                                pushService.send(notification);
                                sentCount++;
                                System.out.println("✅ Push notification sent to: " + user.getEmail());
                            } catch (Exception e) {
                                System.err.println("❌ Failed to send push to " + user.getEmail() + ": " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("ℹ️ User has no subscriptions: " + user.getEmail());
                    }

                    // Send WebSocket notification count update
                    webSocketNotificationController.sendNotificationCountUpdate(user.getId());

                } catch (Exception e) {
                    System.err.println("❌ Error processing user " + user.getEmail() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            response.put("status", "success");
            response.put("message", "Notification sent successfully to " + users.size() + " " + request.getRole() + " users");
            response.put("totalUsers", users.size());
            response.put("pushMessagesSaved", pushMessageCount);
            response.put("webSocketSent", webSocketCount);
            response.put("pushNotificationsSent", sentCount);
            response.put("role", request.getRole());

            System.out.println("✅ sendToRoles completed successfully");
            System.out.println("📊 Final response: " + response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in sendToRoles: " + e.getMessage());
            e.printStackTrace();

            response.put("status", "error");
            response.put("message", "Failed to send notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String sanitize(String input) {
        if (input == null) return "";

        // Remove ALL control characters + escape JSON-sensitive characters
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replaceAll("[\\x00-\\x1F]", "");  // remove all control chars (0–31)
    }

    @DeleteMapping("/{messageId}/delete")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId) {
        try {
            pushMessageService.deleteMessage(messageId);
            return ResponseEntity.ok().body(
                    Map.of(
                            "success", true,
                            "message", "Message deleted successfully",
                            "deletedId", messageId
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete message: " + e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/user/{userId}/deleteAll")
    public ResponseEntity<?> deleteAllUserMessages(@PathVariable String userId) {
        try {
            int deletedCount = pushMessageService.deleteAllUserMessages(userId);
            return ResponseEntity.ok().body(
                    Map.of(
                            "success", true,
                            "message", "All messages deleted successfully",
                            "deletedCount", deletedCount
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete all messages: " + e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/user/{userId}/readDelete")
    public ResponseEntity<?> deleteReadMessages(@PathVariable String userId) {
        try {
            int deletedCount = pushMessageService.deleteReadMessages(userId);
            return ResponseEntity.ok().body(
                    Map.of(
                            "success", true,
                            "message", "Read messages deleted successfully",
                            "deletedCount", deletedCount
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete read messages: " + e.getMessage()
                    ));
        }
    }

}