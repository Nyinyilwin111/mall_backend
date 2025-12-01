//package com.sein_gar_har.Services.implementation;
//
//import com.sein_gar_har.RepositoryMain.SpaceRepository;
//import com.sein_gar_har.RepositoryMain.UtilityRepository;
//import com.sein_gar_har.Services.UtilityService;
//import com.sein_gar_har.Services.AuditLogService;
//import com.sein_gar_har.dto.request.UtilityRequestDTO;
//import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
//import com.sein_gar_har.dto.response.UtilityResponseDTO;
//import com.sein_gar_har.entity.Space;
//import com.sein_gar_har.entity.Utility;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class UtilityServiceImpl implements UtilityService {
//
//    @Autowired
//    private UtilityRepository utilityRepository;
//
//    @Autowired
//    private SpaceRepository spaceRepository;
//
//    @Autowired
//    private HttpServletRequest request;
//
//    private final AuditLogService auditLogService;
//
//    private UtilityResponseDTO convertToDTO(Utility utility) {
//        UtilityResponseDTO dto = new UtilityResponseDTO();
//        dto.setUtilityId(utility.getUtilityId());
//        dto.setSpaceId(utility.getSpace().getSpaceId());
//        dto.setSpaceCode(utility.getSpace().getSpaceCode());
//        dto.setSpaceLocation(utility.getSpace().getLocation());
//        dto.setUtilityType(utility.getUtilityType());
//        dto.setDescription(utility.getDescription());
//        dto.setAmount(utility.getAmount());
//        dto.setDueDate(utility.getDueDate());
//        dto.setBillingPeriod(utility.getBillingPeriod());
//        dto.setUsageUnit(utility.getUsageUnit());
//        dto.setPreviousReading(utility.getPreviousReading());
//        dto.setCurrentReading(utility.getCurrentReading());
//        dto.setUsageAmount(utility.getUsageAmount());
//        dto.setRecordStatus(utility.getRecordStatus());
//        dto.setCreatedAt(utility.getCreatedAt());
//        dto.setUpdatedAt(utility.getUpdatedAt());
//        return dto;
//    }
//
//    @Override
//    @Transactional
//    public UtilityResponseDTO createUtility(UtilityRequestDTO utilityRequestDTO) {
//        // ✅ IMPROVED: User context detection
//        String currentUser = getCurrentUserWithEnhancedStrategies();
//        System.out.println("=== UTILITY CREATE ===");
//        System.out.println("Detected User: " + currentUser);
//        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);
//
//        try {
//            Space space = spaceRepository.findById(utilityRequestDTO.getSpaceId())
//                    .orElseThrow(() -> new RuntimeException("Space not found with id: " + utilityRequestDTO.getSpaceId()));
//
//            Utility utility = new Utility();
//            utility.setSpace(space);
//            utility.setUtilityType(utilityRequestDTO.getUtilityType());
//            utility.setDescription(utilityRequestDTO.getDescription());
//            utility.setDueDate(utilityRequestDTO.getDueDate());
//            utility.setBillingPeriod(utilityRequestDTO.getBillingPeriod());
//            utility.setUsageUnit(utilityRequestDTO.getUsageUnit());
//            utility.setPreviousReading(utilityRequestDTO.getPreviousReading());
//            utility.setCurrentReading(utilityRequestDTO.getCurrentReading());
//            utility.setAmount(utilityRequestDTO.getAmount());
//            utility.calculateUsageAmount();
//
//            Utility savedUtility = utilityRepository.save(utility);
//
//            // ✅ AUDIT LOG: Utility Created
//            Map<String, Object> newData = new HashMap<>();
//            newData.put("utilityId", savedUtility.getUtilityId());
//            newData.put("spaceId", savedUtility.getSpace().getSpaceId().toString());
//            newData.put("spaceCode", savedUtility.getSpace().getSpaceCode());
//            newData.put("utilityType", savedUtility.getUtilityType());
//            newData.put("amount", savedUtility.getAmount());
//            newData.put("dueDate", savedUtility.getDueDate());
//            newData.put("previousReading", savedUtility.getPreviousReading());
//            newData.put("currentReading", savedUtility.getCurrentReading());
//            newData.put("usageAmount", savedUtility.getUsageAmount());
//
//            auditLogService.logCreate("Utility", savedUtility.getUtilityId().toString(), newData);
//
//            return convertToDTO(savedUtility);
//        } finally {
//            // ✅ CLEAR: User context after operation
//            AuditLogService.clearMaintenanceOperationUser();
//        }
//    }
//
//    @Override
//    public List<UtilityResponseDTO> getAllUtilities() {
//        return utilityRepository.findAll()
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public UtilityResponseDTO getUtilityById(Long id) {
//        Utility utility = utilityRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Utility not found with id: " + id));
//        return convertToDTO(utility);
//    }
//
//    @Override
//    public List<UtilityResponseDTO> getUtilitiesBySpaceId(UUID spaceId) {
//        return utilityRepository.findAllBySpaceId(spaceId)
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO) {
//        // ✅ IMPROVED: User context detection
//        String currentUser = getCurrentUserWithEnhancedStrategies();
//        System.out.println("=== UTILITY UPDATE ===");
//        System.out.println("Detected User: " + currentUser);
//        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);
//
//        try {
//            Utility existingUtility = utilityRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Utility not found with id: " + id));
//
//            // ✅ Store old data for audit log
//            Map<String, Object> oldData = new HashMap<>();
//            oldData.put("utilityId", existingUtility.getUtilityId());
//            oldData.put("utilityType", existingUtility.getUtilityType());
//            oldData.put("description", existingUtility.getDescription());
//            oldData.put("amount", existingUtility.getAmount());
//            oldData.put("dueDate", existingUtility.getDueDate());
//            oldData.put("previousReading", existingUtility.getPreviousReading());
//            oldData.put("currentReading", existingUtility.getCurrentReading());
//            oldData.put("usageAmount", existingUtility.getUsageAmount());
//
//            boolean readingsUpdated = false;
//
//            if (utilityUpdateRequestDTO.getUtilityType() != null) {
//                existingUtility.setUtilityType(utilityUpdateRequestDTO.getUtilityType());
//            }
//            if (utilityUpdateRequestDTO.getDescription() != null) {
//                existingUtility.setDescription(utilityUpdateRequestDTO.getDescription());
//            }
//            if (utilityUpdateRequestDTO.getAmount() != null) {
//                existingUtility.setAmount(utilityUpdateRequestDTO.getAmount());
//            }
//            if (utilityUpdateRequestDTO.getDueDate() != null) {
//                existingUtility.setDueDate(utilityUpdateRequestDTO.getDueDate());
//            }
//            if (utilityUpdateRequestDTO.getBillingPeriod() != null) {
//                existingUtility.setBillingPeriod(utilityUpdateRequestDTO.getBillingPeriod());
//            }
//            if (utilityUpdateRequestDTO.getUsageUnit() != null) {
//                existingUtility.setUsageUnit(utilityUpdateRequestDTO.getUsageUnit());
//            }
//            if (utilityUpdateRequestDTO.getPreviousReading() != null) {
//                existingUtility.setPreviousReading(utilityUpdateRequestDTO.getPreviousReading());
//                readingsUpdated = true;
//            }
//            if (utilityUpdateRequestDTO.getCurrentReading() != null) {
//                existingUtility.setCurrentReading(utilityUpdateRequestDTO.getCurrentReading());
//                readingsUpdated = true;
//            }
//
//            if (readingsUpdated) {
//                existingUtility.calculateUsageAmount();
//            }
//
//            existingUtility.preUpdate();
//            Utility updatedUtility = utilityRepository.save(existingUtility);
//
//            // ✅ AUDIT LOG: Utility Updated
//            Map<String, Object> newData = new HashMap<>();
//            newData.put("utilityId", updatedUtility.getUtilityId());
//            newData.put("utilityType", updatedUtility.getUtilityType());
//            newData.put("description", updatedUtility.getDescription());
//            newData.put("amount", updatedUtility.getAmount());
//            newData.put("dueDate", updatedUtility.getDueDate());
//            newData.put("previousReading", updatedUtility.getPreviousReading());
//            newData.put("currentReading", updatedUtility.getCurrentReading());
//            newData.put("usageAmount", updatedUtility.getUsageAmount());
//
//            auditLogService.logUpdate("Utility", id.toString(), oldData, newData);
//
//            return convertToDTO(updatedUtility);
//        } finally {
//            // ✅ CLEAR: User context after operation
//            AuditLogService.clearMaintenanceOperationUser();
//        }
//    }
//
//    @Override
//    @Transactional
//    public boolean deleteUtility(Long id) {
//        // ✅ IMPROVED: User context detection
//        String currentUser = getCurrentUserWithEnhancedStrategies();
//        System.out.println("=== UTILITY DELETE ===");
//        System.out.println("Detected User: " + currentUser);
//        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);
//
//        try {
//            Optional<Utility> utilityOptional = utilityRepository.findById(id);
//            if (utilityOptional.isPresent()) {
//                Utility utility = utilityOptional.get();
//
//                // ✅ Store data for audit log before soft delete
//                Map<String, Object> oldData = new HashMap<>();
//                oldData.put("utilityId", utility.getUtilityId());
//                oldData.put("spaceId", utility.getSpace().getSpaceId().toString());
//                oldData.put("spaceCode", utility.getSpace().getSpaceCode());
//                oldData.put("utilityType", utility.getUtilityType());
//                oldData.put("amount", utility.getAmount());
//                oldData.put("dueDate", utility.getDueDate());
//
//                utility.setRecordStatus("CANCELLED");
//                utility.preUpdate();
//                utilityRepository.save(utility);
//
//                // ✅ AUDIT LOG: Utility Soft Deleted (Cancelled)
//                auditLogService.logAction(
//                        "DELETE",
//                        "Utility",
//                        id.toString(),
//                        oldData,
//                        null
//                );
//
//                return true;
//            }
//            return false;
//        } finally {
//            // ✅ CLEAR: User context after operation
//            AuditLogService.clearMaintenanceOperationUser();
//        }
//    }
//
//    @Override
//    @Transactional
//    public boolean markAsPaid(Long utilityId) {
//        // ✅ IMPROVED: User context detection
//        String currentUser = getCurrentUserWithEnhancedStrategies();
//        System.out.println("=== UTILITY MARK AS PAID ===");
//        System.out.println("Detected User: " + currentUser);
//        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);
//
//        try {
//            Utility utility = utilityRepository.findById(utilityId)
//                    .orElseThrow(() -> new RuntimeException("Utility not found with id: " + utilityId));
//
//            // ✅ AUDIT LOG: Utility Marked as Paid
//            Map<String, Object> details = new HashMap<>();
//            details.put("utilityId", utilityId);
//            details.put("utilityType", utility.getUtilityType());
//            details.put("amount", utility.getAmount());
//            details.put("spaceCode", utility.getSpace().getSpaceCode());
//
//            auditLogService.logAction(
//                    "UTILITY_MARKED_PAID",
//                    "Utility",
//                    utilityId.toString(),
//                    null,
//                    details
//            );
//
//            return true;
//        } finally {
//            // ✅ CLEAR: User context after operation
//            AuditLogService.clearMaintenanceOperationUser();
//        }
//    }
//
//    @Override
//    public BigDecimal getTotalPendingAmountBySpaceId(UUID spaceId) {
//        List<Utility> utilities = utilityRepository.findAllBySpaceId(spaceId);
//        BigDecimal total = utilities.stream()
//                .filter(utility -> utility.getAmount() != null)
//                .map(Utility::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        return total;
//    }
//
//    @Override
//    public List<UtilityResponseDTO> getPendingUtilitiesBySpaceId(UUID spaceId) {
//        return utilityRepository.findAllBySpaceId(spaceId)
//                .stream()
//                .filter(utility -> utility.getAmount() != null && utility.getAmount().compareTo(BigDecimal.ZERO) > 0)
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    // ✅ FIXED: Enhanced user detection with proper header checking
//    private String getCurrentUserWithEnhancedStrategies() {
//        System.out.println("=== ENHANCED USER DETECTION ===");
//
//        // Strategy 1: Check ThreadLocal first (for maintenance operations)
//        String threadLocalUser = getCurrentUserFromThreadLocal();
//        System.out.println("ThreadLocal User: " + threadLocalUser);
//
//        if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
//            return threadLocalUser;
//        }
//
//        // Strategy 2: Check HTTP Headers from frontend
//        String headerUser = getCurrentUserFromHeaders();
//        System.out.println("Header User: " + headerUser);
//
//        if (headerUser != null && !headerUser.trim().isEmpty() && !"System".equals(headerUser)) {
//            return headerUser;
//        }
//
//        // Strategy 3: Security Context
//        String securityUser = getCurrentUserFromSecurityContext();
//        System.out.println("Security Context User: " + securityUser);
//
//        if (securityUser != null && !securityUser.trim().isEmpty() && !"System".equals(securityUser)) {
//            return securityUser;
//        }
//
//        System.out.println("⚠️ No user detected, defaulting to System");
//        return "System";
//    }
//
//    private String getCurrentUserFromThreadLocal() {
//        try {
//            String threadLocalUser = AuditLogService.getCurrentUserFromThreadLocal();
//            System.out.println("ThreadLocal detection - User: " + threadLocalUser);
//
//            if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
//                return threadLocalUser;
//            }
//        } catch (Exception e) {
//            System.err.println("Error in ThreadLocal strategy: " + e.getMessage());
//        }
//        return null;
//    }
//
//    private String getCurrentUserFromHeaders() {
//        try {
//            if (request != null) {
//                // Check for the custom headers sent by frontend
//                String userContext = request.getHeader("X-User-Context");
//                String userEmail = request.getHeader("X-User-Email");
//
//                System.out.println("Header - X-User-Context: " + userContext);
//                System.out.println("Header - X-User-Email: " + userEmail);
//
//                if (userContext != null && !userContext.trim().isEmpty() && !"System".equals(userContext)) {
//                    return userContext;
//                }
//
//
//            }
//        } catch (Exception e) {
//            System.err.println("Error in header strategy: " + e.getMessage());
//        }
//        return null;
//    }
//
//    private String getCurrentUserFromSecurityContext() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            System.out.println("Security Context - Authentication: " + authentication);
//
//            if (authentication != null && authentication.isAuthenticated()) {
//                Object principal = authentication.getPrincipal();
//                System.out.println("Security Context - Principal: " + principal);
//
//                String username = extractUsernameFromPrincipal(principal);
//                System.out.println("Security Context - Extracted Username: " + username);
//
//                if (isValidUsername(username)) {
//                    return username;
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error in Security Context strategy: " + e.getMessage());
//        }
//        return "System";
//    }
//
//    private String extractUsernameFromPrincipal(Object principal) {
//        if (principal instanceof UserDetails) {
//            return ((UserDetails) principal).getUsername();
//        } else if (principal instanceof String) {
//            return (String) principal;
//        }
//        return null;
//    }
//
//    private boolean isValidUsername(String username) {
//        return username != null &&
//                !username.trim().isEmpty() &&
//                !"anonymousUser".equals(username) &&
//                !"system".equalsIgnoreCase(username) &&
//                !"null".equalsIgnoreCase(username);
//    }
//}



package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.LeaseRepository;
import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.RepositoryMain.UtilityRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.UtilityService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.dto.request.UtilityRequestDTO;
import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
import com.sein_gar_har.dto.response.UtilityResponseDTO;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.entity.Utility;
import com.sein_gar_har.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityServiceImpl implements UtilityService {

    @Autowired
    private UtilityRepository utilityRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    // ✅ ADDED: User Repository
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    private final AuditLogService auditLogService;

    // ✅ ADDED: Method to get tenant name by ID
    private String getTenantNameById(UUID tenantId) {
        if (tenantId == null) {
            return null;
        }
        try {
            Optional<User> tenant = userRepository.findById(tenantId);
            return tenant.map(User::getFullName).orElse("Unknown Tenant");
        } catch (Exception e) {
            System.err.println("Error fetching tenant name for ID: " + tenantId + " - " + e.getMessage());
            return "Unknown Tenant";
        }
    }

    // ✅ ADDED: Method to get tenant email by ID
    private String getTenantEmailById(UUID tenantId) {
        if (tenantId == null) {
            return null;
        }
        try {
            Optional<User> tenant = userRepository.findById(tenantId);
            return tenant.map(User::getEmail).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching tenant email for ID: " + tenantId + " - " + e.getMessage());
            return null;
        }
    }

    // ✅ UPDATED: Convert to DTO with tenant information
    private UtilityResponseDTO convertToDTO(Utility utility) {
        UtilityResponseDTO dto = new UtilityResponseDTO();
        dto.setUtilityId(utility.getUtilityId());
        dto.setSpaceId(utility.getSpace().getSpaceId());
        dto.setSpaceCode(utility.getSpace().getSpaceCode());
        dto.setSpaceLocation(utility.getSpace().getLocation());

        // ✅ ADDED: Tenant information
        dto.setTenantId(utility.getTenantId());
        if (utility.getTenantId() != null) {
            dto.setTenantName(getTenantNameById(utility.getTenantId()));
            dto.setTenantEmail(getTenantEmailById(utility.getTenantId()));
        }

        dto.setUtilityType(utility.getUtilityType());
        dto.setDescription(utility.getDescription());
        dto.setAmount(utility.getAmount());
        dto.setDueDate(utility.getDueDate());
        dto.setBillingPeriod(utility.getBillingPeriod());
        dto.setUsageUnit(utility.getUsageUnit());
        dto.setPreviousReading(utility.getPreviousReading());
        dto.setCurrentReading(utility.getCurrentReading());
        dto.setUsageAmount(utility.getUsageAmount());
        dto.setRecordStatus(utility.getRecordStatus());
        dto.setCreatedAt(utility.getCreatedAt());
        dto.setUpdatedAt(utility.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public UtilityResponseDTO createUtility(UtilityRequestDTO utilityRequestDTO) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== UTILITY CREATE ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Space space = spaceRepository.findById(utilityRequestDTO.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Space not found with id: " + utilityRequestDTO.getSpaceId()));

            Utility utility = new Utility();
            utility.setSpace(space);

            // ✅ AUTO-ASSIGN TENANT: Find tenant from active lease
            UUID tenantId = findTenantIdForSpace(utilityRequestDTO.getSpaceId());
            utility.setTenantId(tenantId);

            utility.setUtilityType(utilityRequestDTO.getUtilityType());
            utility.setDescription(utilityRequestDTO.getDescription());
            utility.setDueDate(utilityRequestDTO.getDueDate());
            utility.setBillingPeriod(utilityRequestDTO.getBillingPeriod());
            utility.setUsageUnit(utilityRequestDTO.getUsageUnit());
            utility.setPreviousReading(utilityRequestDTO.getPreviousReading());
            utility.setCurrentReading(utilityRequestDTO.getCurrentReading());
            utility.setAmount(utilityRequestDTO.getAmount());
            utility.calculateUsageAmount();

            Utility savedUtility = utilityRepository.save(utility);

            // ✅ AUDIT LOG: Utility Created
            Map<String, Object> newData = new HashMap<>();
            newData.put("utilityId", savedUtility.getUtilityId());
            newData.put("spaceId", savedUtility.getSpace().getSpaceId().toString());
            newData.put("spaceCode", savedUtility.getSpace().getSpaceCode());
            newData.put("tenantId", savedUtility.getTenantId() != null ? savedUtility.getTenantId().toString() : "No Tenant");
            newData.put("tenantName", getTenantNameById(savedUtility.getTenantId()));
            newData.put("utilityType", savedUtility.getUtilityType());
            newData.put("amount", savedUtility.getAmount());
            newData.put("dueDate", savedUtility.getDueDate());
            newData.put("previousReading", savedUtility.getPreviousReading());
            newData.put("currentReading", savedUtility.getCurrentReading());
            newData.put("usageAmount", savedUtility.getUsageAmount());

            auditLogService.logCreate("Utility", savedUtility.getUtilityId().toString(), newData);

            return convertToDTO(savedUtility);
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    // ✅ ADDED: Method to find tenant ID for space
    private UUID findTenantIdForSpace(UUID spaceId) {
        try {
            Optional<UUID> tenantIdOpt = leaseRepository.findTenantIdBySpaceId(spaceId);
            if (tenantIdOpt.isPresent()) {
                UUID tenantId = tenantIdOpt.get();
                System.out.println("✅ Found tenant for space " + spaceId + ": " + tenantId);
                return tenantId;
            } else {
                System.out.println("⚠️ No active lease found for space: " + spaceId);
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error finding tenant for space " + spaceId + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<UtilityResponseDTO> getAllUtilities() {
        return utilityRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UtilityResponseDTO getUtilityById(Long id) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utility not found with id: " + id));
        return convertToDTO(utility);
    }

    @Override
    public List<UtilityResponseDTO> getUtilitiesBySpaceId(UUID spaceId) {
        return utilityRepository.findAllBySpaceId(spaceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ ADDED: Get utilities by tenant ID
    @Override
    public List<UtilityResponseDTO> getUtilitiesByTenantId(UUID tenantId) {
        try {
            System.out.println("🔍 Getting utilities for tenant ID: " + tenantId);

            // Get all utilities and filter by tenant ID
            List<Utility> allUtilities = utilityRepository.findAll();
            List<Utility> tenantUtilities = allUtilities.stream()
                    .filter(utility -> utility.getTenantId() != null && utility.getTenantId().equals(tenantId))
                    .collect(Collectors.toList());

            System.out.println("✅ Found " + tenantUtilities.size() + " utilities for tenant: " + tenantId);
            return tenantUtilities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("❌ Error getting utilities for tenant " + tenantId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // ✅ ADDED: Get total pending amount by tenant ID
    @Override
    public BigDecimal getTotalPendingAmountByTenantId(UUID tenantId) {
        try {
            List<UtilityResponseDTO> tenantUtilities = getUtilitiesByTenantId(tenantId);
            BigDecimal total = tenantUtilities.stream()
                    .filter(utility -> utility.getAmount() != null)
                    .map(UtilityResponseDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("✅ Total pending amount for tenant " + tenantId + ": " + total);
            return total;

        } catch (Exception e) {
            System.err.println("❌ Error calculating total pending amount for tenant " + tenantId + ": " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional
    public UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== UTILITY UPDATE ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Utility existingUtility = utilityRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Utility not found with id: " + id));

            // ✅ Store old data for audit log
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("utilityId", existingUtility.getUtilityId());
            oldData.put("utilityType", existingUtility.getUtilityType());
            oldData.put("description", existingUtility.getDescription());
            oldData.put("amount", existingUtility.getAmount());
            oldData.put("dueDate", existingUtility.getDueDate());
            oldData.put("previousReading", existingUtility.getPreviousReading());
            oldData.put("currentReading", existingUtility.getCurrentReading());
            oldData.put("usageAmount", existingUtility.getUsageAmount());
            oldData.put("tenantId", existingUtility.getTenantId());
            oldData.put("tenantName", getTenantNameById(existingUtility.getTenantId()));

            boolean readingsUpdated = false;

            if (utilityUpdateRequestDTO.getUtilityType() != null) {
                existingUtility.setUtilityType(utilityUpdateRequestDTO.getUtilityType());
            }
            if (utilityUpdateRequestDTO.getDescription() != null) {
                existingUtility.setDescription(utilityUpdateRequestDTO.getDescription());
            }
            if (utilityUpdateRequestDTO.getAmount() != null) {
                existingUtility.setAmount(utilityUpdateRequestDTO.getAmount());
            }
            if (utilityUpdateRequestDTO.getDueDate() != null) {
                existingUtility.setDueDate(utilityUpdateRequestDTO.getDueDate());
            }
            if (utilityUpdateRequestDTO.getBillingPeriod() != null) {
                existingUtility.setBillingPeriod(utilityUpdateRequestDTO.getBillingPeriod());
            }
            if (utilityUpdateRequestDTO.getUsageUnit() != null) {
                existingUtility.setUsageUnit(utilityUpdateRequestDTO.getUsageUnit());
            }
            if (utilityUpdateRequestDTO.getPreviousReading() != null) {
                existingUtility.setPreviousReading(utilityUpdateRequestDTO.getPreviousReading());
                readingsUpdated = true;
            }
            if (utilityUpdateRequestDTO.getCurrentReading() != null) {
                existingUtility.setCurrentReading(utilityUpdateRequestDTO.getCurrentReading());
                readingsUpdated = true;
            }

            if (readingsUpdated) {
                existingUtility.calculateUsageAmount();
            }

            existingUtility.preUpdate();
            Utility updatedUtility = utilityRepository.save(existingUtility);

            // ✅ AUDIT LOG: Utility Updated
            Map<String, Object> newData = new HashMap<>();
            newData.put("utilityId", updatedUtility.getUtilityId());
            newData.put("utilityType", updatedUtility.getUtilityType());
            newData.put("description", updatedUtility.getDescription());
            newData.put("amount", updatedUtility.getAmount());
            newData.put("dueDate", updatedUtility.getDueDate());
            newData.put("previousReading", updatedUtility.getPreviousReading());
            newData.put("currentReading", updatedUtility.getCurrentReading());
            newData.put("usageAmount", updatedUtility.getUsageAmount());
            newData.put("tenantId", updatedUtility.getTenantId());
            newData.put("tenantName", getTenantNameById(updatedUtility.getTenantId()));

            auditLogService.logUpdate("Utility", id.toString(), oldData, newData);

            return convertToDTO(updatedUtility);
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean deleteUtility(Long id) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== UTILITY DELETE ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Optional<Utility> utilityOptional = utilityRepository.findById(id);
            if (utilityOptional.isPresent()) {
                Utility utility = utilityOptional.get();

                // ✅ Store data for audit log before soft delete
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("utilityId", utility.getUtilityId());
                oldData.put("spaceId", utility.getSpace().getSpaceId().toString());
                oldData.put("spaceCode", utility.getSpace().getSpaceCode());
                oldData.put("tenantId", utility.getTenantId() != null ? utility.getTenantId().toString() : "No Tenant");
                oldData.put("tenantName", getTenantNameById(utility.getTenantId()));
                oldData.put("utilityType", utility.getUtilityType());
                oldData.put("amount", utility.getAmount());
                oldData.put("dueDate", utility.getDueDate());

                utility.setRecordStatus("CANCELLED");
                utility.preUpdate();
                utilityRepository.save(utility);

                // ✅ AUDIT LOG: Utility Soft Deleted (Cancelled)
                auditLogService.logAction(
                        "DELETE",
                        "Utility",
                        id.toString(),
                        oldData,
                        null
                );

                return true;
            }
            return false;
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean markAsPaid(Long utilityId) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== UTILITY MARK AS PAID ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Utility utility = utilityRepository.findById(utilityId)
                    .orElseThrow(() -> new RuntimeException("Utility not found with id: " + utilityId));

            // ✅ AUDIT LOG: Utility Marked as Paid
            Map<String, Object> details = new HashMap<>();
            details.put("utilityId", utilityId);
            details.put("utilityType", utility.getUtilityType());
            details.put("amount", utility.getAmount());
            details.put("spaceCode", utility.getSpace().getSpaceCode());
            details.put("tenantId", utility.getTenantId() != null ? utility.getTenantId().toString() : "No Tenant");
            details.put("tenantName", getTenantNameById(utility.getTenantId()));

            auditLogService.logAction(
                    "UTILITY_MARKED_PAID",
                    "Utility",
                    utilityId.toString(),
                    null,
                    details
            );

            return true;
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    public BigDecimal getTotalPendingAmountBySpaceId(UUID spaceId) {
        List<Utility> utilities = utilityRepository.findAllBySpaceId(spaceId);
        BigDecimal total = utilities.stream()
                .filter(utility -> utility.getAmount() != null)
                .map(Utility::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    @Override
    public List<UtilityResponseDTO> getPendingUtilitiesBySpaceId(UUID spaceId) {
        return utilityRepository.findAllBySpaceId(spaceId)
                .stream()
                .filter(utility -> utility.getAmount() != null && utility.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // User detection methods (keep existing)
    private String getCurrentUserWithEnhancedStrategies() {
        System.out.println("=== ENHANCED USER DETECTION ===");

        // Strategy 1: Check ThreadLocal first (for maintenance operations)
        String threadLocalUser = getCurrentUserFromThreadLocal();
        System.out.println("ThreadLocal User: " + threadLocalUser);

        if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
            return threadLocalUser;
        }

        // Strategy 2: Check HTTP Headers from frontend
        String headerUser = getCurrentUserFromHeaders();
        System.out.println("Header User: " + headerUser);

        if (headerUser != null && !headerUser.trim().isEmpty() && !"System".equals(headerUser)) {
            return headerUser;
        }

        // Strategy 3: Security Context
        String securityUser = getCurrentUserFromSecurityContext();
        System.out.println("Security Context User: " + securityUser);

        if (securityUser != null && !securityUser.trim().isEmpty() && !"System".equals(securityUser)) {
            return securityUser;
        }

        System.out.println("⚠️ No user detected, defaulting to System");
        return "System";
    }

    private String getCurrentUserFromThreadLocal() {
        try {
            String threadLocalUser = AuditLogService.getCurrentUserFromThreadLocal();
            System.out.println("ThreadLocal detection - User: " + threadLocalUser);

            if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
                return threadLocalUser;
            }
        } catch (Exception e) {
            System.err.println("Error in ThreadLocal strategy: " + e.getMessage());
        }
        return null;
    }

    private String getCurrentUserFromHeaders() {
        try {
            if (request != null) {
                // Check for the custom headers sent by frontend
                String userContext = request.getHeader("X-User-Context");
                String userEmail = request.getHeader("X-User-Email");

                System.out.println("Header - X-User-Context: " + userContext);
                System.out.println("Header - X-User-Email: " + userEmail);

                if (userContext != null && !userContext.trim().isEmpty() && !"System".equals(userContext)) {
                    return userContext;
                }

                if (userEmail != null && !userEmail.trim().isEmpty() && !"System".equals(userEmail)) {
                    return userEmail;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in header strategy: " + e.getMessage());
        }
        return null;
    }

    private String getCurrentUserFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Security Context - Authentication: " + authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                System.out.println("Security Context - Principal: " + principal);

                String username = extractUsernameFromPrincipal(principal);
                System.out.println("Security Context - Extracted Username: " + username);

                if (isValidUsername(username)) {
                    return username;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in Security Context strategy: " + e.getMessage());
        }
        return "System";
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
}