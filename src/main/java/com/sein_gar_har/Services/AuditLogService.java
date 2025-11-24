package com.sein_gar_har.Services;

import com.sein_gar_har.auditEntity.AuditLog;
import com.sein_gar_har.RepositoryAudit.AuditLogRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.websocket.AuditStompSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditStompSender auditStompSender;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    // ThreadLocal for storing current user context
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserFullName = new ThreadLocal<>();

    // Thread-local storage for branch operation users
    private static final ThreadLocal<String> branchOperationUser = new ThreadLocal<>();

    // Set user context for maintenance operations
    public static void setMaintenanceOperationUser(String username, String fullName) {
        currentUser.set(username);
        currentUserFullName.set(fullName);
        System.out.println("=== SET MAINTENANCE OPERATION USER: " + fullName + " ===");
    }

    // Clear user context
    public static void clearMaintenanceOperationUser() {
        currentUser.remove();
        currentUserFullName.remove();
        System.out.println("=== CLEARED MAINTENANCE OPERATION USER ===");
    }

    // Set branch operation user
    public static void setBranchOperationUser(String userFullName) {
        branchOperationUser.set(userFullName);
        System.out.println("=== SET BRANCH OPERATION USER: " + userFullName + " ===");
    }

    // Clear branch operation user
    public static void clearBranchOperationUser() {
        branchOperationUser.remove();
    }

    @Transactional("auditTransactionManager")
    public void logAction(String action, String tableName, String recordId,
                          Object oldData, Object newData) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setTableAffected(tableName);
            auditLog.setRecordId(recordId);
            auditLog.setTimestamp(LocalDateTime.now());

            // Get current user who performed the action - with enhanced detection
            String currentUser = getCurrentUserFullNameEnhanced();
            auditLog.setPerformedBy(currentUser);

            // Request information
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            } else {
                auditLog.setIpAddress("N/A");
                auditLog.setUserAgent("N/A");
            }

            // Convert data to JSON
            if (oldData != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldData));
            }
            if (newData != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newData));
            }

            // Generate user-friendly message
            String friendlyMessage = generateUserFriendlyMessage(action, tableName, recordId, oldData, newData, currentUser);
            auditLog.setUserFriendlyMessage(friendlyMessage);

            // Save to audit database
            AuditLog savedLog = auditLogRepository.save(auditLog);

            // Broadcast via WebSocket
            auditStompSender.broadcast(savedLog);

            System.out.println("✅ Audit log saved: " + friendlyMessage);

        } catch (Exception e) {
            System.err.println("❌ Audit logging failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enhanced method to get current user with multiple fallback strategies
     */
    private String getCurrentUserFullNameEnhanced() {
        try {
            System.out.println("=== ENHANCED USER DETECTION ===");

            // Strategy 1: Check ThreadLocal first (for maintenance operations)
            String threadLocalUser = getCurrentUserFromThreadLocal();
            System.out.println("ThreadLocal User: " + threadLocalUser);

            if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
                return threadLocalUser;
            }

            // Strategy 2: Try SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                System.out.println("Principal: " + principal);
                System.out.println("Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));

                String username = extractUsernameFromPrincipal(principal);
                System.out.println("Extracted username: " + username);

                if (isValidUsername(username)) {
                    String userFullName = findUserFullName(username);
                    if (!"System".equals(userFullName)) {
                        System.out.println("Found user: " + userFullName);
                        return userFullName;
                    }
                }
            }

            // Strategy 3: Check if this is a branch operation
            String branchOperationUser = getBranchOperationUser();
            if (branchOperationUser != null) {
                System.out.println("Found branch operation user: " + branchOperationUser);
                return branchOperationUser;
            }

            // Strategy 4: Last resort - check recent logs for the same IP
            String recentUser = findRecentUserFromIp();
            if (recentUser != null) {
                System.out.println("Found recent user from IP: " + recentUser);
                return recentUser;
            }

            System.out.println("No user found, defaulting to System");
            return "System";

        } catch (Exception e) {
            System.err.println("Error in enhanced user detection: " + e.getMessage());
            return "System";
        }
    }

    private String extractUsernameFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    private boolean isValidUsername(String username) {
        return username != null &&
                !username.trim().isEmpty() &&
                !"anonymousUser".equals(username) &&
                !"system".equalsIgnoreCase(username) &&
                !"null".equalsIgnoreCase(username);
    }

    private String findUserFullName(String username) {
        try {
            // Try by email first
            Optional<User> user = userRepository.findByEmail(username);
            if (user.isPresent()) {
                String fullName = user.get().getFullName();
                return (fullName != null && !fullName.trim().isEmpty()) ? fullName : user.get().getEmail();
            }

            // Try by fullName
            user = userRepository.findByFullName(username);
            if (user.isPresent()) {
                String fullName = user.get().getFullName();
                return (fullName != null && !fullName.trim().isEmpty()) ? fullName : user.get().getEmail();
            }

            // Return username as fallback
            return username;
        } catch (Exception e) {
            System.err.println("Error finding user full name: " + e.getMessage());
            return "System";
        }
    }

    private String getBranchOperationUser() {
        try {
            String user = branchOperationUser.get();
            if (user != null && !user.trim().isEmpty()) {
                return user;
            }
        } catch (Exception e) {
            System.err.println("Error getting branch operation user: " + e.getMessage());
        }
        return null;
    }

    private String findRecentUserFromIp() {
        try {
            if (request != null) {
                String ipAddress = getClientIpAddress();
                // Look for recent audit logs from the same IP in the last 5 minutes
                LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
                // This would require a custom repository method, so we'll skip for now
            }
        } catch (Exception e) {
            // Ignore errors in this fallback
        }
        return null;
    }

    private String generateUserFriendlyMessage(String action, String tableName, String recordId,
                                               Object oldData, Object newData, String performedBy) {
        try {
            // Convert data to maps for easier processing
            Map<String, Object> oldMap = oldData != null ?
                    objectMapper.convertValue(oldData, new TypeReference<Map<String, Object>>() {}) : null;
            Map<String, Object> newMap = newData != null ?
                    objectMapper.convertValue(newData, new TypeReference<Map<String, Object>>() {}) : null;

            // Payment-specific messages
            if ("Payment".equals(tableName)) {
                return generatePaymentMessage(action, oldMap, newMap, performedBy);
            }

            // Existing logic for other entities...
            switch (action) {
                case "CREATE":
                    return generateCreateMessage(tableName, recordId, newData, performedBy);
                case "UPDATE":
                    return generateUpdateMessage(tableName, recordId, oldData, newData, performedBy);
                case "DELETE":
                    return generateDeleteMessage(tableName, recordId, oldData, performedBy);
                case "LOGIN":
                    return generateLoginMessage(tableName, recordId, newData, performedBy);
                case "LOGIN_FAILED":
                    return generateLoginFailedMessage(tableName, recordId, newData, performedBy);
                default:
                    if ("System".equals(performedBy)) {
                        return String.format("System performed %s on %s", action, tableName);
                    }
                    return String.format("%s performed %s on %s", performedBy, action, tableName);
            }
        } catch (Exception e) {
            if ("System".equals(performedBy)) {
                return String.format("System performed %s on %s", action, tableName);
            }
            return String.format("%s performed %s on %s", performedBy, action, tableName);
        }
    }

    // Payment-specific message generator
    private String generatePaymentMessage(String action, Map<String, Object> oldMap,
                                          Map<String, Object> newMap, String performedBy) {
        try {
            Map<String, Object> data = action.equals("CREATE") ? newMap :
                    action.equals("DELETE") ? oldMap : newMap;

            if (data == null) {
                return String.format("💰 %s %s payment", performedBy, action.toLowerCase());
            }

            String amount = data.containsKey("amount") ?
                    String.valueOf(data.get("amount")) : "Unknown";
            String paymentType = data.containsKey("paymentType") ?
                    String.valueOf(data.get("paymentType")) : "Payment";
            String title = data.containsKey("title") ?
                    String.valueOf(data.get("title")) : "Payment";

            switch (action) {
                case "CREATE":
                    if ("LEASE".equals(paymentType)) {
                        String leaseId = data.containsKey("leaseId") ?
                                String.valueOf(data.get("leaseId")) : "Unknown";
                        return String.format("💰 %s created Lease Payment for Lease #%s (Amount: $%s)",
                                performedBy, leaseId, amount);
                    } else if ("UTILITY".equals(paymentType)) {
                        String utilityType = data.containsKey("utilityType") ?
                                String.valueOf(data.get("utilityType")) : "Utility";
                        return String.format("💰 %s created %s Utility Payment (Amount: $%s)",
                                performedBy, utilityType, amount);
                    }
                    return String.format("💰 %s created payment (Amount: $%s)", performedBy, amount);

                case "UPDATE":
                    // Check if this is a status update
                    if (newMap != null && newMap.containsKey("oldStatus") && newMap.containsKey("newStatus")) {
                        String oldStatus = String.valueOf(newMap.get("oldStatus"));
                        String newStatus = String.valueOf(newMap.get("newStatus"));

                        if ("LEASE".equals(paymentType)) {
                            String leaseId = data.containsKey("leaseId") ?
                                    String.valueOf(data.get("leaseId")) : "Unknown";
                            return String.format("🔄 %s changed Lease Payment status from %s to %s (Lease #%s, Amount: $%s)",
                                    performedBy, oldStatus, newStatus, leaseId, amount);
                        } else if ("UTILITY".equals(paymentType)) {
                            String utilityType = data.containsKey("utilityType") ?
                                    String.valueOf(data.get("utilityType")) : "Utility";
                            return String.format("🔄 %s changed Utility Payment status from %s to %s (%s, Amount: $%s)",
                                    performedBy, oldStatus, newStatus, utilityType, amount);
                        }
                        return String.format("🔄 %s changed payment status from %s to %s (Amount: $%s)",
                                performedBy, oldStatus, newStatus, amount);
                    }

                    // Regular update
                    if ("LEASE".equals(paymentType)) {
                        String leaseId = data.containsKey("leaseId") ?
                                String.valueOf(data.get("leaseId")) : "Unknown";
                        return String.format("📝 %s updated Lease Payment for Lease #%s (Amount: $%s)",
                                performedBy, leaseId, amount);
                    } else if ("UTILITY".equals(paymentType)) {
                        String utilityType = data.containsKey("utilityType") ?
                                String.valueOf(data.get("utilityType")) : "Utility";
                        return String.format("📝 %s updated %s Utility Payment (Amount: $%s)",
                                performedBy, utilityType, amount);
                    }
                    return String.format("📝 %s updated payment (Amount: $%s)", performedBy, amount);

                case "DELETE":
                    if ("LEASE".equals(paymentType)) {
                        String leaseId = data.containsKey("leaseId") ?
                                String.valueOf(data.get("leaseId")) : "Unknown";
                        return String.format("🗑️ %s deleted Lease Payment for Lease #%s (Amount: $%s)",
                                performedBy, leaseId, amount);
                    } else if ("UTILITY".equals(paymentType)) {
                        String utilityType = data.containsKey("utilityType") ?
                                String.valueOf(data.get("utilityType")) : "Utility";
                        return String.format("🗑️ %s deleted %s Utility Payment (Amount: $%s)",
                                performedBy, utilityType, amount);
                    }
                    return String.format("🗑️ %s deleted payment (Amount: $%s)", performedBy, amount);

                default:
                    return String.format("💰 %s %s payment (Amount: $%s)", performedBy, action.toLowerCase(), amount);
            }
        } catch (Exception e) {
            return String.format("💰 %s %s payment", performedBy, action.toLowerCase());
        }
    }

    private String generateCreateMessage(String tableName, String recordId, Object newData, String performedBy) {
        try {
            Map<String, Object> data = objectMapper.convertValue(newData, new TypeReference<Map<String, Object>>() {});

            switch (tableName) {
                case "Lease":
                    return generateLeaseCreateMessage(data, performedBy);

                case "User":
                    String userName = getStringValue(data, "fullName");
                    String userEmail = getStringValue(data, "email");
                    if (!userName.isEmpty()) {
                        return String.format("👤 User '%s' (Email: %s) was created by %s",
                                userName, userEmail, performedBy);
                    } else {
                        return String.format("👤 User with email '%s' was created by %s",
                                userEmail, performedBy);
                    }

                case "Role":
                    String roleName = getStringValue(data, "name");
                    Set<String> permissions = getPermissionsSet(data);
                    if (!roleName.isEmpty()) {
                        if (!permissions.isEmpty()) {
                            return String.format("🎭 Role '%s' with permissions %s was created by %s",
                                    roleName, permissions, performedBy);
                        } else {
                            return String.format("🎭 Role '%s' was created by %s", roleName, performedBy);
                        }
                    } else {
                        return String.format("🎭 Role was created by %s", performedBy);
                    }

                case "Permission":
                    String permName = getStringValue(data, "name");
                    if (!permName.isEmpty()) {
                        return String.format("🔐 Permission '%s' was created by %s", permName, performedBy);
                    } else {
                        return String.format("🔐 Permission was created by %s", performedBy);
                    }

                case "Branch":
                    String branchName = getStringValue(data, "name");
                    String branchAddress = getStringValue(data, "address");
                    if (!branchName.isEmpty()) {
                        return String.format("🏢 Branch '%s' (Address: %s) was created by %s",
                                branchName, branchAddress, performedBy);
                    } else {
                        return String.format("🏢 Branch was created by %s", performedBy);
                    }

                case "Space":
                    String spaceCode = getStringValue(data, "spaceCode");
                    String spaceLocation = getStringValue(data, "location");
                    if (!spaceCode.isEmpty()) {
                        return String.format("🏢 Space '%s' (Location: %s) was created by %s",
                                spaceCode, spaceLocation, performedBy);
                    } else {
                        return String.format("🏢 Space was created by %s", performedBy);
                    }

                case "Floor":
                    String floorLevel = getStringValue(data, "level");
                    String branchId = getStringValue(data, "branchBranchId");
                    if (!floorLevel.isEmpty()) {
                        return String.format("🏗️ Floor 'Level %s' (Branch: %s) was created by %s",
                                floorLevel, branchId, performedBy);
                    } else {
                        return String.format("🏗️ Floor was created by %s", performedBy);
                    }

                case "SpaceType":
                    String spaceTypeName = getStringValue(data, "typeName");
                    if (!spaceTypeName.isEmpty()) {
                        return String.format("📦 Space Type '%s' was created by %s",
                                spaceTypeName, performedBy);
                    } else {
                        return String.format("📦 Space Type was created by %s", performedBy);
                    }

                default:
                    String name = getNameFromData(data);
                    return String.format("📄 %s '%s' was created by %s", tableName, name, performedBy);
            }
        } catch (Exception e) {
            return String.format("📄 New %s was created by %s", tableName, performedBy);
        }
    }

    private String generateUpdateMessage(String tableName, String recordId, Object oldData, Object newData, String performedBy) {
        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldData, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> newMap = objectMapper.convertValue(newData, new TypeReference<Map<String, Object>>() {});

            switch (tableName) {
                case "Lease":
                    return generateLeaseUpdateMessage(oldMap, newMap, performedBy);

                case "User":
                    String oldName = getStringValue(oldMap, "fullName");
                    String newName = getStringValue(newMap, "fullName");
                    String oldEmail = getStringValue(oldMap, "email");
                    String newEmail = getStringValue(newMap, "email");

                    if (!oldName.isEmpty()) {
                        if (!oldName.equals(newName) && !oldEmail.equals(newEmail)) {
                            return String.format("👤 User '%s' was updated by %s: name to '%s', email to '%s'",
                                    oldName, performedBy, newName, newEmail);
                        } else if (!oldName.equals(newName)) {
                            return String.format("👤 User '%s' was updated by %s: name changed to '%s'",
                                    oldName, performedBy, newName);
                        } else if (!oldEmail.equals(newEmail)) {
                            return String.format("👤 User '%s' was updated by %s: email changed to '%s'",
                                    oldName, performedBy, newEmail);
                        } else {
                            // Check for role changes
                            Set<String> oldRoles = getRolesSet(oldMap);
                            Set<String> newRoles = getRolesSet(newMap);
                            if (!oldRoles.equals(newRoles)) {
                                Set<String> addedRoles = new HashSet<>(newRoles);
                                addedRoles.removeAll(oldRoles);
                                Set<String> removedRoles = new HashSet<>(oldRoles);
                                removedRoles.removeAll(newRoles);

                                StringBuilder roleChanges = new StringBuilder();
                                if (!addedRoles.isEmpty()) {
                                    roleChanges.append("added roles: ").append(addedRoles).append(", ");
                                }
                                if (!removedRoles.isEmpty()) {
                                    roleChanges.append("removed roles: ").append(removedRoles).append(", ");
                                }
                                if (roleChanges.length() > 0) {
                                    roleChanges.setLength(roleChanges.length() - 2);
                                    return String.format("👤 User '%s' was updated by %s: %s",
                                            oldName, performedBy, roleChanges.toString());
                                }
                            }
                            return String.format("👤 User '%s' was updated by %s", oldName, performedBy);
                        }
                    } else {
                        return String.format("👤 User was updated by %s", performedBy);
                    }

                case "Role":
                    String roleName = getStringValue(newMap, "name");
                    if (!roleName.isEmpty()) {
                        // Check for permission changes
                        Set<String> oldPermissions = getPermissionsSet(oldMap);
                        Set<String> newPermissions = getPermissionsSet(newMap);

                        if (!oldPermissions.equals(newPermissions)) {
                            Set<String> addedPermissions = new HashSet<>(newPermissions);
                            addedPermissions.removeAll(oldPermissions);
                            Set<String> removedPermissions = new HashSet<>(oldPermissions);
                            removedPermissions.removeAll(newPermissions);

                            StringBuilder permissionChanges = new StringBuilder();
                            if (!addedPermissions.isEmpty()) {
                                permissionChanges.append("added permissions: ").append(addedPermissions).append(", ");
                            }
                            if (!removedPermissions.isEmpty()) {
                                permissionChanges.append("removed permissions: ").append(removedPermissions).append(", ");
                            }
                            if (permissionChanges.length() > 0) {
                                permissionChanges.setLength(permissionChanges.length() - 2);
                                return String.format("🎭 Role '%s' was updated by %s: %s",
                                        roleName, performedBy, permissionChanges.toString());
                            }
                        }
                        return String.format("🎭 Role '%s' was updated by %s", roleName, performedBy);
                    } else {
                        return String.format("🎭 Role was updated by %s", performedBy);
                    }

                case "Branch":
                    String branchName = getStringValue(newMap, "name");
                    String oldBranchName = getStringValue(oldMap, "name");
                    String oldAddress = getStringValue(oldMap, "address");
                    String newAddress = getStringValue(newMap, "address");

                    if (!branchName.isEmpty()) {
                        if (!oldBranchName.equals(branchName) && !oldAddress.equals(newAddress)) {
                            return String.format("🏢 Branch '%s' was updated by %s: name to '%s', address to '%s'",
                                    oldBranchName, performedBy, branchName, newAddress);
                        } else if (!oldBranchName.equals(branchName)) {
                            return String.format("🏢 Branch '%s' was updated by %s: name changed to '%s'",
                                    oldBranchName, performedBy, branchName);
                        } else if (!oldAddress.equals(newAddress)) {
                            return String.format("🏢 Branch '%s' was updated by %s: address changed to '%s'",
                                    branchName, performedBy, newAddress);
                        } else {
                            return String.format("🏢 Branch '%s' was updated by %s", branchName, performedBy);
                        }
                    } else {
                        return String.format("🏢 Branch was updated by %s", performedBy);
                    }

                default:
                    String name = getNameFromData(newMap);
                    return String.format("📝 %s '%s' was updated by %s", tableName, name, performedBy);
            }
        } catch (Exception e) {
            return String.format("📝 %s was updated by %s", tableName, performedBy);
        }
    }

    private String generateDeleteMessage(String tableName, String recordId, Object oldData, String performedBy) {
        try {
            Map<String, Object> data = objectMapper.convertValue(oldData, new TypeReference<Map<String, Object>>() {});

            switch (tableName) {
                case "Lease":
                    return generateLeaseDeleteMessage(data, performedBy);

                case "User":
                    String userName = getStringValue(data, "fullName");
                    String userEmail = getStringValue(data, "email");
                    if (!userName.isEmpty()) {
                        return String.format("🗑️ User '%s' (Email: %s) was deleted by %s",
                                userName, userEmail, performedBy);
                    } else {
                        return String.format("🗑️ User with email '%s' was deleted by %s",
                                userEmail, performedBy);
                    }

                case "Role":
                    String roleName = getStringValue(data, "name");
                    if (!roleName.isEmpty()) {
                        return String.format("🗑️ Role '%s' was deleted by %s", roleName, performedBy);
                    } else {
                        return String.format("🗑️ Role was deleted by %s", performedBy);
                    }

                case "Branch":
                    String branchName = getStringValue(data, "name");
                    String branchAddress = getStringValue(data, "address");
                    if (!branchName.isEmpty()) {
                        return String.format("🗑️ Branch '%s' (Address: %s) was deleted by %s",
                                branchName, branchAddress, performedBy);
                    } else {
                        return String.format("🗑️ Branch was deleted by %s", performedBy);
                    }

                default:
                    String name = getNameFromData(data);
                    return String.format("🗑️ %s '%s' was deleted by %s", tableName, name, performedBy);
            }
        } catch (Exception e) {
            return String.format("🗑️ %s was deleted by %s", tableName, performedBy);
        }
    }

    private String generateLoginMessage(String tableName, String recordId, Object newData, String performedBy) {
        try {
            if (newData != null) {
                Map<String, Object> data = objectMapper.convertValue(newData, new TypeReference<Map<String, Object>>() {});
                String username = getStringValue(data, "username");
                String fullName = getStringValue(data, "fullName");
                String email = getStringValue(data, "email");

                if (!fullName.isEmpty() && !"null".equals(fullName)) {
                    return String.format("🔑 User '%s' (%s) logged in successfully", fullName, email);
                } else if (!username.isEmpty()) {
                    return String.format("🔑 User '%s' logged in successfully", username);
                }
            }
            return String.format("🔑 User logged in successfully");
        } catch (Exception e) {
            return String.format("🔑 User logged in");
        }
    }

    private String generateLoginFailedMessage(String tableName, String recordId, Object newData, String performedBy) {
        try {
            if (newData != null) {
                Map<String, Object> data = objectMapper.convertValue(newData, new TypeReference<Map<String, Object>>() {});
                String username = getStringValue(data, "username");
                String reason = getStringValue(data, "reason");

                if (!username.isEmpty()) {
                    return String.format("🔒 Failed login attempt for user '%s' - %s", username, reason);
                }
            }
            return String.format("🔒 Failed login attempt");
        } catch (Exception e) {
            return String.format("🔒 Failed login attempt");
        }
    }

    // LEASE SPECIFIC MESSAGE GENERATORS
    private String generateLeaseCreateMessage(Map<String, Object> data, String performedBy) {
        String leaseId = getStringValue(data, "leaseId");
        String tenantName = getStringValue(data, "tenantName");
        String spaceName = getStringValue(data, "spaceName");
        String displayName = getStringValue(data, "displayName");

        if (!displayName.isEmpty() && !"Unknown".equals(displayName)) {
            return String.format("📄 Lease '%s' was created by %s", displayName, performedBy);
        } else if (!tenantName.isEmpty() && !spaceName.isEmpty()) {
            return String.format("📄 Lease '%s - %s' was created by %s", tenantName, spaceName, performedBy);
        } else if (!leaseId.isEmpty()) {
            return String.format("📄 Lease #%s was created by %s", leaseId, performedBy);
        } else {
            return String.format("📄 Lease was created by %s", performedBy);
        }
    }

    private String generateLeaseUpdateMessage(Map<String, Object> oldMap, Map<String, Object> newMap, String performedBy) {
        String oldDisplayName = getStringValue(oldMap, "displayName");
        String newDisplayName = getStringValue(newMap, "displayName");
        String leaseId = getStringValue(newMap, "leaseId");

        String displayName = !newDisplayName.isEmpty() ? newDisplayName :
                (!oldDisplayName.isEmpty() ? oldDisplayName :
                        (!leaseId.isEmpty() ? "Lease #" + leaseId : "Lease"));

        // Check for specific field changes
        StringBuilder changes = new StringBuilder();

        // Check status change
        String oldStatus = getStringValue(oldMap, "status");
        String newStatus = getStringValue(newMap, "status");
        if (!oldStatus.equals(newStatus)) {
            changes.append("status from ").append(oldStatus).append(" to ").append(newStatus).append(", ");
        }

        // Check rent amount change
        Double oldRent = getDoubleValue(oldMap, "rentAmount");
        Double newRent = getDoubleValue(newMap, "rentAmount");
        if (oldRent != null && newRent != null && !oldRent.equals(newRent)) {
            changes.append("rent from ").append(oldRent).append(" to ").append(newRent).append(", ");
        }

        if (changes.length() > 0) {
            changes.setLength(changes.length() - 2);
            return String.format("📝 Lease '%s' was updated by %s: %s", displayName, performedBy, changes.toString());
        }

        return String.format("📝 Lease '%s' was updated by %s", displayName, performedBy);
    }

    private String generateLeaseDeleteMessage(Map<String, Object> data, String performedBy) {
        String leaseId = getStringValue(data, "leaseId");
        String tenantName = getStringValue(data, "tenantName");
        String spaceName = getStringValue(data, "spaceName");
        String displayName = getStringValue(data, "displayName");

        if (!displayName.isEmpty() && !"Unknown".equals(displayName)) {
            return String.format("🗑️ Lease '%s' was deleted by %s", displayName, performedBy);
        } else if (!tenantName.isEmpty() && !spaceName.isEmpty()) {
            return String.format("🗑️ Lease '%s - %s' was deleted by %s", tenantName, spaceName, performedBy);
        } else if (!leaseId.isEmpty()) {
            return String.format("🗑️ Lease #%s was deleted by %s", leaseId, performedBy);
        } else {
            return String.format("🗑️ Lease was deleted by %s", performedBy);
        }
    }

    // Helper methods
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? String.valueOf(value) : "";
    }

    private Double getDoubleValue(Map<String, Object> data, String key) {
        try {
            Object value = data.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getNameFromData(Map<String, Object> data) {
        // Payment specific
        if (data.containsKey("paymentId") && data.get("paymentId") != null) {
            String paymentType = data.containsKey("paymentType") ?
                    String.valueOf(data.get("paymentType")) : "Payment";
            String amount = data.containsKey("amount") ?
                    String.valueOf(data.get("amount")) : "Unknown";

            if ("LEASE".equals(paymentType)) {
                String leaseId = data.containsKey("leaseId") ?
                        String.valueOf(data.get("leaseId")) : "Unknown";
                return String.format("Lease Payment #%s", leaseId);
            } else if ("UTILITY".equals(paymentType)) {
                String utilityType = data.containsKey("utilityType") ?
                        String.valueOf(data.get("utilityType")) : "Utility";
                return String.format("%s Payment", utilityType);
            }
            return String.format("Payment #%s", data.get("paymentId"));
        }

        // Lease specific - check for leaseId first
        if (data.containsKey("leaseId") && data.get("leaseId") != null) {
            return "Lease #" + String.valueOf(data.get("leaseId"));
        }
        // Lease - check for displayName
        else if (data.containsKey("displayName") && data.get("displayName") != null) {
            return String.valueOf(data.get("displayName"));
        }
        // Lease - check for tenant and space names
        else if (data.containsKey("tenantName") && data.get("tenantName") != null &&
                data.containsKey("spaceName") && data.get("spaceName") != null) {
            return String.valueOf(data.get("tenantName")) + " - " + String.valueOf(data.get("spaceName"));
        }
        // Space specific
        else if (data.containsKey("spaceCode") && data.get("spaceCode") != null) {
            return String.valueOf(data.get("spaceCode"));
        }
        // Space type specific
        else if (data.containsKey("typeName") && data.get("typeName") != null) {
            return String.valueOf(data.get("typeName"));
        }
        // Floor specific
        else if (data.containsKey("level") && data.get("level") != null) {
            return "Level " + String.valueOf(data.get("level"));
        }
        // User specific
        else if (data.containsKey("fullName") && data.get("fullName") != null) {
            return String.valueOf(data.get("fullName"));
        }
        // General name fields
        else if (data.containsKey("name") && data.get("name") != null) {
            return String.valueOf(data.get("name"));
        }
        // Title fields
        else if (data.containsKey("title") && data.get("title") != null) {
            return String.valueOf(data.get("title"));
        }
        // Email as fallback for users
        else if (data.containsKey("email") && data.get("email") != null) {
            return String.valueOf(data.get("email"));
        }
        else {
            return "Unknown";
        }
    }

    private Set<String> getPermissionsSet(Map<String, Object> data) {
        try {
            Object permissionsObj = data.get("permissions");
            if (permissionsObj instanceof Collection) {
                return new HashSet<>((Collection<String>) permissionsObj);
            }
            return new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private Set<String> getRolesSet(Map<String, Object> data) {
        try {
            Object rolesObj = data.get("roles");
            if (rolesObj instanceof Collection) {
                return new HashSet<>((Collection<String>) rolesObj);
            }
            return new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private Set<String> getBranchesSet(Map<String, Object> data, String key) {
        try {
            Object branchesObj = data.get(key);
            if (branchesObj instanceof Collection) {
                return new HashSet<>((Collection<String>) branchesObj);
            }
            return new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private String getClientIpAddress() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    // Convenience methods
    public void logCreate(String tableName, String recordId, Object newData) {
        logAction("CREATE", tableName, recordId, null, newData);
    }

    public void logUpdate(String tableName, String recordId, Object oldData, Object newData) {
        logAction("UPDATE", tableName, recordId, oldData, newData);
    }

    public void logDelete(String tableName, String recordId, Object oldData) {
        logAction("DELETE", tableName, recordId, oldData, null);
    }

    // Special method for login with fullName
    public void logLogin(String recordId, Object loginData, String fullName) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("LOGIN");
            auditLog.setTableAffected("User");
            auditLog.setRecordId(recordId);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setPerformedBy(fullName);

            // Request information
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            } else {
                auditLog.setIpAddress("N/A");
                auditLog.setUserAgent("N/A");
            }

            // Convert data to JSON
            if (loginData != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(loginData));
            }

            // Generate user-friendly message
            String friendlyMessage = generateLoginMessage("User", recordId, loginData, fullName);
            auditLog.setUserFriendlyMessage(friendlyMessage);

            // Save to audit database
            AuditLog savedLog = auditLogRepository.save(auditLog);
            auditStompSender.broadcast(savedLog);

        } catch (Exception e) {
            System.err.println("❌ Login audit logging failed: " + e.getMessage());
        }
    }

    // Custom action logging
    @Transactional("auditTransactionManager")
    public void logCustomAction(String action, String message, Object details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setTableAffected("Custom");
            auditLog.setRecordId("N/A");
            auditLog.setTimestamp(LocalDateTime.now());

            // Get current user
            String currentUser = getCurrentUserFullNameEnhanced();
            auditLog.setPerformedBy(currentUser);

            // Request information
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            } else {
                auditLog.setIpAddress("N/A");
                auditLog.setUserAgent("N/A");
            }

            // Convert details to JSON
            if (details != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(details));
            }

            // Set user-friendly message
            auditLog.setUserFriendlyMessage(message);

            // Save to audit database
            AuditLog savedLog = auditLogRepository.save(auditLog);

            // Broadcast via WebSocket
            auditStompSender.broadcast(savedLog);

        } catch (Exception e) {
            System.err.println("❌ Custom audit logging failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Static method to get current user from ThreadLocal
    public static String getCurrentUserFromThreadLocal() {
        try {
            // Try full name first
            String fullName = currentUserFullName.get();
            if (fullName != null && !fullName.trim().isEmpty() && !"System".equals(fullName)) {
                return fullName;
            }

            // Then try username
            String username = currentUser.get();
            if (username != null && !username.trim().isEmpty() && !"System".equals(username)) {
                return username;
            }
        } catch (Exception e) {
            System.err.println("Error accessing ThreadLocal: " + e.getMessage());
        }
        return null;
    }
}