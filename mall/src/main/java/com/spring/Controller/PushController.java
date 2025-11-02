package com.spring.Controller;

import com.spring.DTO.request.GetPushMessageDto;
import com.spring.DTO.request.SubscriptionDto;
import com.spring.DTO.response.PushMessageDto;
import com.spring.Entity.*;
import com.spring.Repository.*;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/push")
@CrossOrigin(origins = "*")
public class PushController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    UserService userService;

    @Autowired
    PushMessageService pushMessageService;

    @Value("${vapid.public}")
    private String publicKey;

    @Value("${vapid.private}")
    private String privateKey;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // Save a new subscription from frontend
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody SubscriptionDto dto,
                                                         @RequestParam String userId) {
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

            // FIX: Check if subscription with same endpoint already exists
            boolean subscriptionExists = user.getSubscriptions().stream()
                    .anyMatch(sub -> sub.getEndpoint().equals(newEntity.getEndpoint()));

            if (subscriptionExists) {
                // Subscription already exists, just return success
                System.out.println("Subscription already exists for user: " + user.getEmail());
                response.put("status", "success");
                response.put("message", "Subscription already exists");
                response.put("action", "exists");
                response.put("subscriptionCount", user.getSubscriptions().size());
                return ResponseEntity.ok(response);
            }

            // Remove any existing subscription with different endpoint but same user
            // This ensures only one subscription per user
            if (!user.getSubscriptions().isEmpty()) {
                System.out.println("Replacing old subscription with new one for user: " + user.getEmail());
                user.getSubscriptions().clear();
            }

            // Add the new subscription
            user.getSubscriptions().add(newEntity);
            userRepository.save(user);

            System.out.println("Subscription " + (subscriptionExists ? "updated" : "added") +
                    " for user: " + user.getEmail() + " (Total: " + user.getSubscriptions().size() + ")");

            response.put("status", "success");
            response.put("message", "Subscription processed successfully");
            response.put("action", subscriptionExists ? "updated" : "added");
            response.put("subscriptionCount", user.getSubscriptions().size());
            response.put("userEmail", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error processing subscription: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /** Global broadcast to all subscriptions - FIXED VERSION */
    @PostMapping("/sendAll")
    public Map<String, Object> sendAll(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title");
        String body = payload.get("body");
        String userId = payload.get("sentuserId");

        if (title == null || body == null) {
            response.put("status", "error");
            response.put("message", "Missing title or body");
            return response;
        }

        try {
            // Save push message
            PushMessage message = new PushMessage();
            message.setMessage(title + " - " + body);
            message.setDateTime(LocalDateTime.now());
            message.setSentToAll(true);

            if (userId != null && !userId.isEmpty()) {
                UUID uid = UUID.fromString(userId);
                userRepository.findById(uid).ifPresent(message::setCreatedUserId);
            }

            PushMessage savedMessage = pushMessageService.save(message);

            // Send push notifications
            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            // FIXED: Proper JSON payload format
            String jsonBody = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                    title.replace("\"", "\\\""),
                    body.replace("\"", "\\\""),
                    LocalDateTime.now().toString()
            );

            int sentCount = 0;
            int totalSubscriptions = 0;

            for (User user : userRepository.findAll()) {
                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    totalSubscriptions++;
                    try {
                        Notification notification = new Notification(
                                sub.getEndpoint(),
                                sub.getP256dh(),
                                sub.getAuth(),
                                jsonBody.getBytes("UTF-8")
                        );
                        pushService.send(notification);
                        sentCount++;
                        System.out.println("Push sent to: " + user.getEmail());
                    } catch (Exception e) {
                        System.err.println("Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                        // Remove invalid subscriptions
                        if (e.getMessage().contains("410") || e.getMessage().contains("404")) {
                            user.getSubscriptions().removeIf(s -> s.getEndpoint().equals(sub.getEndpoint()));
                            userRepository.save(user);
                            System.out.println("Removed invalid subscription for: " + user.getEmail());
                        }
                    }
                }
            }

            response.put("status", "success");
            response.put("sent", sentCount);
            response.put("total", totalSubscriptions);
            response.put("messageId", savedMessage.getId());
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /** Branch-specific broadcast - FIXED VERSION */
    @PostMapping("/sendBranch")
    public Map<String, Object> sendBranch(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title");
        String body = payload.get("body");
        String branchIdStr = payload.get("branchId");
        String userIdStr = payload.get("sentuserId");

        if (title == null || body == null || branchIdStr == null) {
            response.put("status", "error");
            response.put("message", "Missing title, body, or branchId");
            return response;
        }

        try {
            UUID branchId = UUID.fromString(branchIdStr);
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Branch not found");
                return response;
            }

            List<User> branchUsers = userRepository.findUsersByBranch(branchOpt.get());
            if (branchUsers.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "No users found in this branch");
                return response;
            }

            // PushService for notifications
            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            int sentCount = 0;
            int totalSubscriptions = 0;

            // FIXED: Proper JSON payload format
            String jsonBody = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                    title.replace("\"", "\\\""),
                    body.replace("\"", "\\\""),
                    LocalDateTime.now().toString()
            );

            // Save a PushMessage for each user and send push notification
            for (User user : branchUsers) {
                PushMessage message = new PushMessage();
                message.setMessage(title + " - " + body);
                message.setDateTime(LocalDateTime.now());
                message.setSentToAll(false);
                message.setBranch(branchOpt.get());
                message.setRecipientUser(user);

                if (userIdStr != null && !userIdStr.isEmpty()) {
                    UUID senderId = UUID.fromString(userIdStr);
                    userRepository.findById(senderId).ifPresent(message::setCreatedUserId);
                }

                PushMessage savedMessage = pushMessageService.save(message);

                // Send web push notification
                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    totalSubscriptions++;
                    try {
                        Notification notification = new Notification(
                                sub.getEndpoint(),
                                sub.getP256dh(),
                                sub.getAuth(),
                                jsonBody.getBytes("UTF-8")
                        );
                        pushService.send(notification);
                        sentCount++;
                        System.out.println("Push sent to branch user: " + user.getEmail());
                    } catch (Exception e) {
                        System.err.println("Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                    }
                }
            }

            response.put("status", "success");
            response.put("sent", sentCount);
            response.put("total", totalSubscriptions);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /** Send to specific user - FIXED VERSION */
    @PostMapping("/sendToUser")
    public Map<String, Object> sendToUser(@RequestBody Map<String, String> payload) {

        System.out.println(" reach into send specific user _________________-");
        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title");
        String body = payload.get("body");
        String userIdStr = payload.get("userId");
        String senderIdStr = payload.get("sentuserId");

        if (title == null || body == null || userIdStr == null) {
            response.put("status", "error");
            response.put("message", "Missing title, body, or userId");
            return response;
        }

        try {
            UUID userId = UUID.fromString(userIdStr);
            Optional<User> userOpt = userRepository.findById(userId);
            System.out.println("get user is : "+userOpt);
            if (userOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "User not found");
                return response;
            }

            User user = userOpt.get();
            if (user.getSubscriptions().isEmpty()) {
                response.put("status", "warning");
                response.put("message", "User has no active subscriptions");
                return response;
            }

            // Save push message
            PushMessage message = new PushMessage();
            message.setMessage(title + " - " + body);
            message.setDateTime(LocalDateTime.now());
            message.setSentToAll(false);
            message.setRecipientUser(user);

            if (senderIdStr != null && !senderIdStr.isEmpty()) {
                UUID senderId = UUID.fromString(senderIdStr);
                userRepository.findById(senderId).ifPresent(message::setCreatedUserId);
            }

            PushMessage savedMessage = pushMessageService.save(message);

            System.out.println("save message info : "+savedMessage);

            // Send push notification
            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:admin@seingahar.com");

            // FIXED: Proper JSON payload format
            String jsonBody = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/sgh.png\",\"badge\":\"/sgh.png\",\"url\":\"/notifications\",\"timestamp\":\"%s\"}",
                    title.replace("\"", "\\\""),
                    body.replace("\"", "\\\""),
                    LocalDateTime.now().toString()
            );

            int sentCount = 0;
            for (SubscriptionEntity sub : user.getSubscriptions()) {
                try {
                    Notification notification = new Notification(
                            sub.getEndpoint(),
                            sub.getP256dh(),
                            sub.getAuth(),
                            jsonBody.getBytes("UTF-8")
                    );
                    pushService.send(notification);
                    sentCount++;
                    System.out.println("Push sent to user: " + user.getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send to user " + user.getEmail() + ": " + e.getMessage());
                }
            }

            response.put("status", "success");
            response.put("sent", sentCount);
            response.put("messageId", savedMessage.getId());
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /** Return public VAPID key for frontend */
    @GetMapping("/vapidPublicKey")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", publicKey);
    }

    // Test endpoint to verify DTO conversion
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetPushMessageDto>> GetUserMessages(@PathVariable String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            List<GetPushMessageDto> messages = pushMessageService.getMessagesForUser(userUuid);

            for (GetPushMessageDto message : messages) {
                System.out.println("Message ID: " + message.getId());
                System.out.println("Message Content: " + message.getMessage());
                System.out.println("Date Time: " + message.getDateTime());
                System.out.println("Sent To All: " + message.isSentToAll());
                System.out.println("Read Status: " + message.isReadby());
                System.out.println("Branch Name: " + message.getBranchName());
                System.out.println("Sender Name: " + message.getSenderName());
                System.out.println("Title: " + message.getTitle()); // If you have getTitle() method
                System.out.println("-----------------------------------");
            }
            // Manual conversion to ensure no serialization issues
            List<GetPushMessageDto> dtos = messages.stream().map(msg -> {
                GetPushMessageDto dto = new GetPushMessageDto();
                dto.setId(msg.getId());
                dto.setMessage(msg.getMessage());
                dto.setDateTime(msg.getDateTime());
                dto.setSentToAll(msg.isSentToAll());
                dto.setReadby(msg.isReadby());
                dto.setBranchName(msg.getBranch() != null ? msg.getBranch().getName() : null);
                dto.setSenderName("Test Sender"); // Hardcode for testing
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    // Mark message as read
    @PutMapping("/message/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String messageId) {
        try {
            UUID messageUuid = UUID.fromString(messageId);
            pushMessageService.markAsRead(messageUuid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Unsubscribe endpoint - FIXED: Use HttpStatus.INTERNAL_SERVER_ERROR */
    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestParam String userId,
                                              @RequestParam String endpoint) {
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
                    return ResponseEntity.ok("Unsubscribed successfully");
                }
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subscription not found");

        } catch (Exception e) {
            // FIX: Use HttpStatus.INTERNAL_SERVER_ERROR instead of HttpInternalServerError
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error unsubscribing: " + e.getMessage());
        }
    }
}