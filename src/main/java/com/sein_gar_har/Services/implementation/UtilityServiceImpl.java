package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.RepositoryMain.UtilityRepository;
import com.sein_gar_har.Services.UtilityService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.dto.request.UtilityRequestDTO;
import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
import com.sein_gar_har.dto.response.UtilityResponseDTO;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.entity.Utility;
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

    private final AuditLogService auditLogService;

    private UtilityResponseDTO convertToDTO(Utility utility) {
        UtilityResponseDTO dto = new UtilityResponseDTO();
        dto.setUtilityId(utility.getUtilityId());
        dto.setSpaceId(utility.getSpace().getSpaceId());
        dto.setSpaceCode(utility.getSpace().getSpaceCode());
        dto.setSpaceLocation(utility.getSpace().getLocation());
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
        // ✅ SET: User context before operation
        String currentUser = getCurrentUserWithMultipleStrategies();
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Space space = spaceRepository.findById(utilityRequestDTO.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Space not found with id: " + utilityRequestDTO.getSpaceId()));

            Utility utility = new Utility();
            utility.setSpace(space);
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
            newData.put("utilityType", savedUtility.getUtilityType());
            newData.put("amount", savedUtility.getAmount());
            newData.put("dueDate", savedUtility.getDueDate());
            newData.put("previousReading", savedUtility.getPreviousReading());
            newData.put("currentReading", savedUtility.getCurrentReading());
            newData.put("usageAmount", savedUtility.getUsageAmount());

            auditLogService.logCreate("Utility", savedUtility.getUtilityId().toString(), newData);

            return convertToDTO(savedUtility);
        } finally {
            // ✅ CLEAR: User context after operation
            AuditLogService.clearMaintenanceOperationUser();
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

    @Override
    @Transactional
    public UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO) {
        // ✅ SET: User context before operation
        String currentUser = getCurrentUserWithMultipleStrategies();
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

            auditLogService.logUpdate("Utility", id.toString(), oldData, newData);

            return convertToDTO(updatedUtility);
        } finally {
            // ✅ CLEAR: User context after operation
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean deleteUtility(Long id) {
        // ✅ SET: User context before operation
        String currentUser = getCurrentUserWithMultipleStrategies();
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
            // ✅ CLEAR: User context after operation
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean markAsPaid(Long utilityId) {
        // ✅ SET: User context before operation
        String currentUser = getCurrentUserWithMultipleStrategies();
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

            auditLogService.logAction(
                    "UTILITY_MARKED_PAID",
                    "Utility",
                    utilityId.toString(),
                    null,
                    details
            );

            return true;
        } finally {
            // ✅ CLEAR: User context after operation
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

    // ✅ ADD: User context methods (copy from BranchServiceImpl)
    private String getCurrentUserWithMultipleStrategies() {
        // Strategy 1: Try Security Context first
        String userFromSecurity = getCurrentUserFromSecurityContext();
        if (!"System".equals(userFromSecurity)) {
            return userFromSecurity;
        }

        // Strategy 2: Try JWT Token from Authorization header
        String userFromJwt = getCurrentUserFromJwtToken();
        if (!"System".equals(userFromJwt)) {
            return userFromJwt;
        }

        // Strategy 3: Last resort - check if there's a test header
        String userFromHeader = getCurrentUserFromCustomHeader();
        if (!"System".equals(userFromHeader)) {
            return userFromHeader;
        }

        return "System";
    }

    private String getCurrentUserFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                return extractUsernameFromPrincipal(principal);
            }
        } catch (Exception e) {
            System.err.println("Error in Security Context strategy: " + e.getMessage());
        }
        return "System";
    }

    private String getCurrentUserFromJwtToken() {
        // Implement based on your JWT token provider
        // This is a simplified version - adjust based on your actual implementation
        try {
            String token = extractTokenFromRequest();
            if (token != null && !token.trim().isEmpty()) {
                // Use your token provider to extract user info
                // This is a placeholder - replace with your actual implementation
                return "UserFromJWT"; // Replace with actual user extraction
            }
        } catch (Exception e) {
            System.err.println("JWT Strategy Error: " + e.getMessage());
        }
        return "System";
    }

    private String getCurrentUserFromCustomHeader() {
        try {
            // Implement based on your HttpServletRequest
            // This is a placeholder - replace with your actual implementation
            return "System";
        } catch (Exception e) {
            System.err.println("Error in custom header strategy: " + e.getMessage());
        }
        return "System";
    }

    private String extractTokenFromRequest() {
        // Implement token extraction from HttpServletRequest
        // This is a placeholder
        return null;
    }

    private String extractUsernameFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }
}