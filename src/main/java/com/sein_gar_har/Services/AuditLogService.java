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
    // ✅ ADD these methods to your existing AuditLogService class

    // ThreadLocal for storing current user context
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserFullName = new ThreadLocal<>();

    // Set user context for maintenance operations
    public static void setMaintenanceOperationUser(String username, String fullName) {
        currentUser.set(username);
        currentUserFullName.set(fullName);
    }

    // Clear user context
    public static void clearMaintenanceOperationUser() {
        currentUser.remove();
        currentUserFullName.remove();
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
            // Strategy 1: Try SecurityContext first
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("=== ENHANCED USER DETECTION ===");
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

            // Strategy 2: Check if this is a branch operation and try to get from thread local
            String branchOperationUser = getBranchOperationUser();
            if (branchOperationUser != null) {
                System.out.println("Found branch operation user: " + branchOperationUser);
                return branchOperationUser;
            }

            // Strategy 3: Last resort - check recent logs for the same IP
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
                !"system".equalsIgnoreCase(username);
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

    /**
     * Thread-local storage for branch operation users
     */
    private static final ThreadLocal<String> branchOperationUser = new ThreadLocal<>();

    public static void setBranchOperationUser(String userFullName) {
        branchOperationUser.set(userFullName);
        System.out.println("=== SET BRANCH OPERATION USER: " + userFullName + " ===");
    }

    public static void clearBranchOperationUser() {
        branchOperationUser.remove();
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
                // You can implement this if needed
            }
        } catch (Exception e) {
            // Ignore errors in this fallback
        }
        return null;
    }

    private String generateUserFriendlyMessage(String action, String tableName, String recordId,
                                               Object oldData, Object newData, String performedBy) {
        try {
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

                case "Space":
                    String oldSpaceCode = getStringValue(oldMap, "spaceCode");
                    String newSpaceCode = getStringValue(newMap, "spaceCode");
                    String oldSpaceLocation = getStringValue(oldMap, "location");
                    String newSpaceLocation = getStringValue(newMap, "location");

                    if (!newSpaceCode.isEmpty()) {
                        if (!oldSpaceCode.equals(newSpaceCode) && !oldSpaceLocation.equals(newSpaceLocation)) {
                            return String.format("🏢 Space '%s' was updated by %s: code to '%s', location to '%s'",
                                    oldSpaceCode, performedBy, newSpaceCode, newSpaceLocation);
                        } else if (!oldSpaceCode.equals(newSpaceCode)) {
                            return String.format("🏢 Space '%s' was updated by %s: code changed to '%s'",
                                    oldSpaceCode, performedBy, newSpaceCode);
                        } else if (!oldSpaceLocation.equals(newSpaceLocation)) {
                            return String.format("🏢 Space '%s' was updated by %s: location changed to '%s'",
                                    newSpaceCode, performedBy, newSpaceLocation);
                        } else {
                            // Check for other changes
                            Double oldPrice = getDoubleValue(oldMap, "price");
                            Double newPrice = getDoubleValue(newMap, "price");
                            String oldStatus = getStringValue(oldMap, "status");
                            String newStatus = getStringValue(newMap, "status");

                            StringBuilder changes = new StringBuilder();
                            if (oldPrice != null && newPrice != null && !oldPrice.equals(newPrice)) {
                                changes.append("price from ").append(oldPrice).append(" to ").append(newPrice).append(", ");
                            }
                            if (!oldStatus.equals(newStatus)) {
                                changes.append("status from ").append(oldStatus).append(" to ").append(newStatus).append(", ");
                            }

                            if (changes.length() > 0) {
                                changes.setLength(changes.length() - 2);
                                return String.format("🏢 Space '%s' was updated by %s: %s",
                                        newSpaceCode, performedBy, changes.toString());
                            }
                            return String.format("🏢 Space '%s' was updated by %s", newSpaceCode, performedBy);
                        }
                    } else {
                        return String.format("🏢 Space was updated by %s", performedBy);
                    }

                case "Floor":
                    String oldFloorLevel = getStringValue(oldMap, "level");
                    String newFloorLevel = getStringValue(newMap, "level");
                    String oldBranchId = getStringValue(oldMap, "branchBranchId");
                    String newBranchId = getStringValue(newMap, "branchBranchId");

                    if (!newFloorLevel.isEmpty()) {
                        if (!oldFloorLevel.equals(newFloorLevel) && !oldBranchId.equals(newBranchId)) {
                            return String.format("🏗️ Floor 'Level %s' was updated by %s: level to '%s', branch to '%s'",
                                    oldFloorLevel, performedBy, newFloorLevel, newBranchId);
                        } else if (!oldFloorLevel.equals(newFloorLevel)) {
                            return String.format("🏗️ Floor 'Level %s' was updated by %s: level changed to '%s'",
                                    oldFloorLevel, performedBy, newFloorLevel);
                        } else if (!oldBranchId.equals(newBranchId)) {
                            return String.format("🏗️ Floor 'Level %s' was updated by %s: branch changed to '%s'",
                                    newFloorLevel, performedBy, newBranchId);
                        } else {
                            return String.format("🏗️ Floor 'Level %s' was updated by %s", newFloorLevel, performedBy);
                        }
                    } else {
                        return String.format("🏗️ Floor was updated by %s", performedBy);
                    }

                case "SpaceType":
                    String oldSpaceTypeName = getStringValue(oldMap, "typeName");
                    String newSpaceTypeName = getStringValue(newMap, "typeName");
                    String oldDescription = getStringValue(oldMap, "description");
                    String newDescription = getStringValue(newMap, "description");

                    if (!newSpaceTypeName.isEmpty()) {
                        if (!oldSpaceTypeName.equals(newSpaceTypeName) && !oldDescription.equals(newDescription)) {
                            return String.format("📦 Space Type '%s' was updated by %s: name to '%s', description to '%s'",
                                    oldSpaceTypeName, performedBy, newSpaceTypeName, newDescription);
                        } else if (!oldSpaceTypeName.equals(newSpaceTypeName)) {
                            return String.format("📦 Space Type '%s' was updated by %s: name changed to '%s'",
                                    oldSpaceTypeName, performedBy, newSpaceTypeName);
                        } else if (!oldDescription.equals(newDescription)) {
                            return String.format("📦 Space Type '%s' was updated by %s: description updated",
                                    newSpaceTypeName, performedBy);
                        } else {
                            return String.format("📦 Space Type '%s' was updated by %s", newSpaceTypeName, performedBy);
                        }
                    } else {
                        return String.format("📦 Space Type was updated by %s", performedBy);
                    }

                case "UserBranch":
                    String userFullName = getStringValue(newMap, "userFullName");
                    String userEmail = getStringValue(newMap, "userEmail");

                    // Check for assigned branches
                    Set<String> assignedBranches = getBranchesSet(newMap, "assignedBranches");
                    if (!assignedBranches.isEmpty()) {
                        return String.format("👤🏢 User '%s' (%s) was assigned branches: %s by %s",
                                userFullName, userEmail, assignedBranches, performedBy);
                    }

                    // Check for removed branches
                    Set<String> removedBranches = getBranchesSet(newMap, "removedBranches");
                    if (!removedBranches.isEmpty()) {
                        return String.format("👤🏢 User '%s' (%s) had branches removed: %s by %s",
                                userFullName, userEmail, removedBranches, performedBy);
                    }

                    // Check for updated branches
                    Set<String> newBranches = getBranchesSet(newMap, "newBranches");
                    Set<String> oldBranches = getBranchesSet(oldMap, "oldBranches");
                    if (!newBranches.isEmpty() && !oldBranches.isEmpty()) {
                        return String.format("👤🏢 User '%s' (%s) branch assignment updated from %s to %s by %s",
                                userFullName, userEmail, oldBranches, newBranches, performedBy);
                    }

                    return String.format("👤🏢 User branch assignment updated for '%s' by %s", userFullName, performedBy);

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

                case "Permission":
                    String permName = getStringValue(data, "name");
                    if (!permName.isEmpty()) {
                        return String.format("🗑️ Permission '%s' was deleted by %s", permName, performedBy);
                    } else {
                        return String.format("🗑️ Permission was deleted by %s", performedBy);
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

                case "Space":
                    String deletedSpaceCode = getStringValue(data, "spaceCode");
                    String deletedSpaceLocation = getStringValue(data, "location");
                    if (!deletedSpaceCode.isEmpty()) {
                        return String.format("🗑️ Space '%s' (Location: %s) was deleted by %s",
                                deletedSpaceCode, deletedSpaceLocation, performedBy);
                    } else {
                        return String.format("🗑️ Space was deleted by %s", performedBy);
                    }

                case "Floor":
                    String deletedFloorLevel = getStringValue(data, "level");
                    String deletedBranchId = getStringValue(data, "branchBranchId");
                    if (!deletedFloorLevel.isEmpty()) {
                        return String.format("🗑️ Floor 'Level %s' (Branch: %s) was deleted by %s",
                                deletedFloorLevel, deletedBranchId, performedBy);
                    } else {
                        return String.format("🗑️ Floor was deleted by %s", performedBy);
                    }

                case "SpaceType":
                    String deletedSpaceTypeName = getStringValue(data, "typeName");
                    if (!deletedSpaceTypeName.isEmpty()) {
                        return String.format("🗑️ Space Type '%s' was deleted by %s",
                                deletedSpaceTypeName, performedBy);
                    } else {
                        return String.format("🗑️ Space Type was deleted by %s", performedBy);
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

        // Check dates change
        String oldStartDate = getStringValue(oldMap, "startDate");
        String newStartDate = getStringValue(newMap, "startDate");
        String oldEndDate = getStringValue(oldMap, "endDate");
        String newEndDate = getStringValue(newMap, "endDate");

        if (!oldStartDate.equals(newStartDate)) {
            changes.append("start date updated, ");
        }
        if (!oldEndDate.equals(newEndDate)) {
            changes.append("end date updated, ");
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
        // Try different key patterns for different entities

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
        // Lease - check for tenant name only
        else if (data.containsKey("tenantName") && data.get("tenantName") != null) {
            return String.valueOf(data.get("tenantName"));
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
            auditLog.setPerformedBy(fullName); // Use the actual fullName

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

    // Add this method to your AuditLogService class
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
    // Add this method to your existing AuditLogService class
    private String generateUserFriendlyMessage(String action, String tableName, String recordId,
                                               Map<String, Object> oldData, Map<String, Object> newData,
                                               String performedBy) {
        switch (action) {
            case "CREATE":
                if ("Payment".equals(tableName)) {
                    String amount = newData != null && newData.get("amount") != null ?
                            newData.get("amount").toString() : "Unknown";
                    String paymentType = newData != null ? (String) newData.get("paymentType") : "Unknown";
                    if ("LEASE".equals(paymentType)) {
                        String leaseId = newData != null && newData.get("leaseId") != null ?
                                newData.get("leaseId").toString() : "Unknown";
                        return String.format("💰 %s created LEASE payment for Lease %s (Amount: %s)",
                                performedBy, leaseId, amount);
                    } else if ("UTILITY".equals(paymentType)) {
                        String utilityType = newData != null ? (String) newData.get("utilityType") : "Unknown";
                        return String.format("💰 %s created UTILITY payment for %s (Amount: %s)",
                                performedBy, utilityType, amount);
                    }
                    return String.format("💰 %s created %s payment (Amount: %s)",
                            performedBy, paymentType, amount);
                }
                break;

            case "UPDATE":
                if ("Payment".equals(tableName)) {
                    String amount = newData != null && newData.get("amount") != null ?
                            newData.get("amount").toString() : "Unknown";
                    String paymentType = newData != null ? (String) newData.get("paymentType") : "Unknown";

                    if ("LEASE".equals(paymentType)) {
                        String leaseId = newData != null && newData.get("leaseId") != null ?
                                newData.get("leaseId").toString() : "Unknown";
                        return String.format("📝 %s updated LEASE payment for Lease %s (Amount: %s)",
                                performedBy, leaseId, amount);
                    } else if ("UTILITY".equals(paymentType)) {
                        String utilityType = newData != null ? (String) newData.get("utilityType") : "Unknown";
                        return String.format("📝 %s updated UTILITY payment for %s (Amount: %s)",
                                performedBy, utilityType, amount);
                    }
                    return String.format("📝 %s updated payment (Amount: %s)", performedBy, amount);
                }
                break;

            case "DELETE":
                if ("Payment".equals(tableName)) {
                    String amount = oldData != null && oldData.get("amount") != null ?
                            oldData.get("amount").toString() : "Unknown";
                    String paymentType = oldData != null ? (String) oldData.get("paymentType") : "Unknown";

                    if ("LEASE".equals(paymentType)) {
                        String leaseId = oldData != null && oldData.get("leaseId") != null ?
                                oldData.get("leaseId").toString() : "Unknown";
                        return String.format("🗑️ %s deleted LEASE payment for Lease %s (Amount: %s)",
                                performedBy, leaseId, amount);
                    } else if ("UTILITY".equals(paymentType)) {
                        String utilityType = oldData != null ? (String) oldData.get("utilityType") : "Unknown";
                        return String.format("🗑️ %s deleted UTILITY payment for %s (Amount: %s)",
                                performedBy, utilityType, amount);
                    }
                    return String.format("🗑️ %s deleted payment (Amount: %s)", performedBy, amount);
                }
                break;

            case "PAYMENT_STATUS_UPDATE":
                String oldStatus = newData != null ? (String) newData.get("oldStatus") : "Unknown";
                String newStatus = newData != null ? (String) newData.get("newStatus") : "Unknown";
                String paymentAmount = newData != null && newData.get("amount") != null ?
                        newData.get("amount").toString() : "Unknown";
                String paymentType = newData != null ? (String) newData.get("paymentType") : "Unknown";

                if ("LEASE".equals(paymentType)) {
                    String leaseId = newData != null && newData.get("leaseId") != null ?
                            newData.get("leaseId").toString() : "Unknown";
                    return String.format("🔄 %s changed LEASE payment status from %s to %s (Lease: %s, Amount: %s)",
                            performedBy, oldStatus, newStatus, leaseId, paymentAmount);
                } else if ("UTILITY".equals(paymentType)) {
                    String utilityType = newData != null ? (String) newData.get("utilityType") : "Unknown";
                    return String.format("🔄 %s changed UTILITY payment status from %s to %s (%s, Amount: %s)",
                            performedBy, oldStatus, newStatus, utilityType, paymentAmount);
                } else {
                    return String.format("🔄 %s changed payment status from %s to %s (Amount: %s)",
                            performedBy, oldStatus, newStatus, paymentAmount);
                }

            case "UTILITY_CANCELLED":
                String utilityType = oldData != null ? (String) oldData.get("utilityType") : "Unknown";
                String amount = oldData != null && oldData.get("amount") != null ?
                        oldData.get("amount").toString() : "Unknown";
                return String.format("❌ %s cancelled utility: %s (Amount: %s)",
                        performedBy, utilityType, amount);

            case "UTILITY_MARKED_PAID":
                String paidUtilityType = newData != null ? (String) newData.get("utilityType") : "Unknown";
                String paidAmount = newData != null && newData.get("amount") != null ?
                        newData.get("amount").toString() : "Unknown";
                return String.format("✅ %s marked utility as paid: %s (Amount: %s)",
                        performedBy, paidUtilityType, paidAmount);

            default:
                return String.format("%s performed %s on %s: %s",
                        performedBy, action, tableName, recordId);
        }

        return String.format("%s performed %s on %s: %s",
                performedBy, action, tableName, recordId);
    }

    // Add this static method to AuditLogService class
    public static String getCurrentUserFromThreadLocal() {
        try {
            // Access ThreadLocal variables using reflection
            java.lang.reflect.Field currentUserField = AuditLogService.class.getDeclaredField("currentUser");
            java.lang.reflect.Field currentUserFullNameField = AuditLogService.class.getDeclaredField("currentUserFullName");

            currentUserField.setAccessible(true);
            currentUserFullNameField.setAccessible(true);

            ThreadLocal<String> currentUserTL = (ThreadLocal<String>) currentUserField.get(null);
            ThreadLocal<String> currentUserFullNameTL = (ThreadLocal<String>) currentUserFullNameField.get(null);

            String fullName = currentUserFullNameTL != null ? currentUserFullNameTL.get() : null;
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            }

            String username = currentUserTL != null ? currentUserTL.get() : null;
            if (username != null && !username.trim().isEmpty()) {
                return username;
            }
        } catch (Exception e) {
            System.err.println("Error accessing ThreadLocal: " + e.getMessage());
        }
        return null;
    }
}