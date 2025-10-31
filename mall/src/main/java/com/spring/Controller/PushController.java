package com.spring.Controller;

import com.spring.DTO.request.GetPushMessageDto;
import com.spring.DTO.request.SubscriptionDto;
import com.spring.Entity.*;
import com.spring.Repository.*;
import com.spring.Services.PushMessageService;
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
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/push")
@CrossOrigin(origins = "*")
public class PushController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    PushMessageService pushMessageService;

    private final List<SubscriptionDto> subscriptions = new CopyOnWriteArrayList<>();

    @Value("${vapid.public}")
    private String publicKey;

    @Value("${vapid.private}")
    private String privateKey;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /** Save a new subscription from frontend */
    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscriptionDto dto,
                                            @RequestParam String userId) {
        try {
            // Validate & convert userId to UUID safely
            UUID uuid = UUID.fromString(userId);

            Optional<User> userOpt = userRepository.findById(uuid);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("❌ User not found for ID: " + userId);
            }

            User user = userOpt.get();
            SubscriptionEntity entity = SubscriptionMapper.toEntity(dto);
            user.getSubscriptions().add(entity);
            userRepository.save(user);

            System.out.println("✅ Subscription added for user: " + user.getEmail());
            return ResponseEntity.ok("✅ Subscription saved successfully");

        } catch (IllegalArgumentException ex) {
            // Thrown if UUID.fromString() fails
            System.err.println("⚠️ Invalid userId format: " + userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid userId format: must be a valid UUID");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving subscription: " + e.getMessage());
        }
    }

    /** Global broadcast to all subscriptions */
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

            pushMessageService.save(message);
            // Send notifications
            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:you@example.com");

            String jsonBody = "{\"title\": \"" + title + "\", \"body\": \"" + body + "\"}";

            for (User user : userRepository.findAll()) {
                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    Notification notification = new Notification(
                            sub.getEndpoint(),
                            sub.getP256dh(),
                            sub.getAuth(),
                            jsonBody.getBytes()
                    );
                    try {
                        pushService.send(notification);
                    } catch (Exception ignored) {}
                }
            }

            response.put("status", "success");
            response.put("sent", userRepository.count());
            response.put("messageId", message.getId());
            return response;

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /** Branch-specific broadcast */
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

            PushMessage message = new PushMessage();
            message.setMessage(title + " - " + body);
            message.setDateTime(LocalDateTime.now());
            message.setSentToAll(false);
            message.setBranch(branchOpt.get());

            if (userIdStr != null && !userIdStr.isEmpty()) {
                UUID userId = UUID.fromString(userIdStr);
                userRepository.findById(userId).ifPresent(message::setCreatedUserId);
            }

            pushMessageService.save(message);

            // Send notifications only to users in the branch
            List<User> branchUsers = userRepository.findByBranch(branchOpt.get());

            PushService pushService = new PushService();
            pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
            pushService.setPublicKey(Utils.loadPublicKey(publicKey));
            pushService.setSubject("mailto:you@example.com");

            String jsonBody = "{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}";

            for (User user : branchUsers) {
                for (SubscriptionEntity sub : user.getSubscriptions()) {
                    Notification notification = new Notification(
                            sub.getEndpoint(),
                            sub.getP256dh(),
                            sub.getAuth(),
                            jsonBody.getBytes()
                    );
                    try {
                        pushService.send(notification);
                    } catch (Exception ignored) {}
                }
            }

            response.put("status", "success");
            response.put("sent", branchUsers.size());
            response.put("messageId", message.getId());
            return response;

        } catch (Exception e) {
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

    @GetMapping("/user/{userId}")
    public List<GetPushMessageDto> getUserMessages(@PathVariable UUID userId) {
        return pushMessageService.getMessagesForUser(userId);
    }

}
