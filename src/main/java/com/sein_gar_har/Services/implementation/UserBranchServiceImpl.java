package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.UserBranchService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.dto.request.UserBranchRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.dto.response.UserResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBranchServiceImpl implements UserBranchService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BranchRepository branchRepository;

    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserResponseDTO assignBranchesToUser(UserBranchRequestDTO userBranchRequestDTO) {
        UUID userId = userBranchRequestDTO.getUserId();
        Set<Long> branchIds = userBranchRequestDTO.getBranchIds();

        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get current user for audit log
        String currentUser = getCurrentUserFullName();
        System.out.println("=== ASSIGN BRANCHES - Current User: " + currentUser + " ===");

        // Store old data for audit log
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("userId", user.getId());
        oldData.put("userFullName", user.getFullName());
        oldData.put("userEmail", user.getEmail());
        oldData.put("currentBranches", user.getBranches().stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));

        // Find all branches by their IDs
        Set<Branch> branchesToAdd = new HashSet<>(branchRepository.findByIds(branchIds));

        if (branchesToAdd.size() != branchIds.size()) {
            throw new RuntimeException("One or more branches not found");
        }

        // Add branches to user
        for (Branch branch : branchesToAdd) {
            user.addBranch(branch);
        }

        User savedUser = userRepository.save(user);
        log.info("Assigned {} branches to user: {}", branchesToAdd.size(), user.getEmail());

        // Store new data for audit log
        Map<String, Object> newData = new HashMap<>();
        newData.put("userId", savedUser.getId());
        newData.put("userFullName", savedUser.getFullName());
        newData.put("userEmail", savedUser.getEmail());
        newData.put("assignedBranches", branchesToAdd.stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));
        newData.put("totalBranchesAfter", savedUser.getBranches().stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));

        // Use direct audit logging
        logUserBranchActionDirectly("UPDATE", userId.toString(), oldData, newData, currentUser);

        return convertToUserResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO removeBranchesFromUser(UserBranchRequestDTO userBranchRequestDTO) {
        UUID userId = userBranchRequestDTO.getUserId();
        Set<Long> branchIds = userBranchRequestDTO.getBranchIds();

        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get current user for audit log
        String currentUser = getCurrentUserFullName();
        System.out.println("=== REMOVE BRANCHES - Current User: " + currentUser + " ===");

        // Store old data for audit log
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("userId", user.getId());
        oldData.put("userFullName", user.getFullName());
        oldData.put("userEmail", user.getEmail());
        oldData.put("currentBranches", user.getBranches().stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));

        // Find all branches by their IDs
        Set<Branch> branchesToRemove = new HashSet<>(branchRepository.findByIds(branchIds));

        // Remove branches from user
        for (Branch branch : branchesToRemove) {
            user.removeBranch(branch);
        }

        User savedUser = userRepository.save(user);
        log.info("Removed {} branches from user: {}", branchesToRemove.size(), user.getEmail());

        // Store new data for audit log
        Map<String, Object> newData = new HashMap<>();
        newData.put("userId", savedUser.getId());
        newData.put("userFullName", savedUser.getFullName());
        newData.put("userEmail", savedUser.getEmail());
        newData.put("removedBranches", branchesToRemove.stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));
        newData.put("totalBranchesAfter", savedUser.getBranches().stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));

        // Use direct audit logging
        logUserBranchActionDirectly("UPDATE", userId.toString(), oldData, newData, currentUser);

        return convertToUserResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserWithBranches(UUID userId) {
        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToUserResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserBranches(UserBranchRequestDTO userBranchRequestDTO) {
        UUID userId = userBranchRequestDTO.getUserId();
        Set<Long> branchIds = userBranchRequestDTO.getBranchIds();

        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get current user for audit log
        String currentUser = getCurrentUserFullName();
        System.out.println("=== UPDATE USER BRANCHES - Current User: " + currentUser + " ===");

        // Store old data for audit log
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("userId", user.getId());
        oldData.put("userFullName", user.getFullName());
        oldData.put("userEmail", user.getEmail());
        oldData.put("oldBranches", user.getBranches().stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));

        // Clear existing branches
        user.getBranches().clear();

        // Find and add new branches
        Set<Branch> newBranches = new HashSet<>(branchRepository.findByIds(branchIds));

        if (newBranches.size() != branchIds.size()) {
            throw new RuntimeException("One or more branches not found");
        }

        for (Branch branch : newBranches) {
            user.addBranch(branch);
        }

        User savedUser = userRepository.save(user);
        log.info("Updated branches for user: {}, now has {} branches", user.getEmail(), newBranches.size());

        // Store new data for audit log
        Map<String, Object> newData = new HashMap<>();
        newData.put("userId", savedUser.getId());
        newData.put("userFullName", savedUser.getFullName());
        newData.put("userEmail", savedUser.getEmail());
        newData.put("newBranches", newBranches.stream()
                .map(Branch::getName)
                .collect(Collectors.toSet()));

        // Use direct audit logging
        logUserBranchActionDirectly("UPDATE", userId.toString(), oldData, newData, currentUser);

        return convertToUserResponseDTO(savedUser);
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFullName(user.getFullName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setEnabled(user.isEnabled());

        // Convert roles
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        userResponseDTO.setRoles(roleNames);

        // Convert branches
        Set<BranchResponseDTO> branchDTOs = user.getBranches().stream()
                .map(this::convertToBranchResponseDTO)
                .collect(Collectors.toSet());
        userResponseDTO.setBranches(branchDTOs);

        return userResponseDTO;
    }

    private BranchResponseDTO convertToBranchResponseDTO(Branch branch) {
        BranchResponseDTO branchResponseDTO = new BranchResponseDTO();
        branchResponseDTO.setId(branch.getId());
        branchResponseDTO.setName(branch.getName());
        branchResponseDTO.setAddress(branch.getAddress());
        branchResponseDTO.setPhoneNumber(branch.getPhoneNumber());
        branchResponseDTO.setCreatedAt(branch.getCreatedAt());
        branchResponseDTO.setUpdatedAt(branch.getUpdatedAt());
        return branchResponseDTO;
    }

    // Helper method to get current user full name
    private String getCurrentUserFullName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                String username = "";

                if (principal instanceof UserDetails) {
                    username = ((UserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    username = (String) principal;
                }

                // Skip anonymousUser
                if ("anonymousUser".equals(username)) {
                    return "";
                }

                // Try to find user by email first
                Optional<User> user = userRepository.findByEmail(username);

                // If not found by email, try by fullName
                if (user.isEmpty()) {
                    user = userRepository.findByFullName(username);
                }

                if (user.isPresent()) {
                    String fullName = user.get().getFullName();
                    return fullName != null ? fullName : username;
                } else {
                    return username;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in getCurrentUserFullName: " + e.getMessage());
        }

        return "";
    }

    // Direct audit logging method for user branch actions
    private void logUserBranchActionDirectly(String action, String recordId, Object oldData, Object newData, String performedBy) {
        try {
            // If performedBy is empty, try to get it again
            if (performedBy == null || performedBy.isEmpty()) {
                performedBy = getCurrentUserFullName();
            }

            // If still empty, use a fallback
            if (performedBy == null || performedBy.isEmpty()) {
                performedBy = "Unknown User";
            }

            System.out.println("=== Direct UserBranch Audit Log ===");
            System.out.println("Action: " + action);
            System.out.println("RecordId: " + recordId);
            System.out.println("PerformedBy: " + performedBy);

            // Use the audit log service
            auditLogService.logUpdate("UserBranch", recordId, oldData, newData);

        } catch (Exception e) {
            System.err.println("Error in direct user branch audit logging: " + e.getMessage());
            e.printStackTrace();
        }
    }
}