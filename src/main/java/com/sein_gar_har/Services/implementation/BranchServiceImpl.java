package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.CreateBranchRequestDTO;
import com.sein_gar_har.dto.request.UpdateBranchRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;

    private final AuditLogService auditLogService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private HttpServletRequest request;

    @Override
    public List<BranchResponseDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponseDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return convertToResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponseDTO createBranch(CreateBranchRequestDTO requestDTO) {
        // Get current user with multiple fallback strategies
        String currentUser = getCurrentUserWithMultipleStrategies();
        System.out.println("=== CREATE BRANCH - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            if (branchRepository.existsByName(requestDTO.getName())) {
                throw new RuntimeException("Branch name already exists: " + requestDTO.getName());
            }

            Branch branch = new Branch();
            branch.setName(requestDTO.getName());
            branch.setAddress(requestDTO.getAddress());
            branch.setPhoneNumber(requestDTO.getPhoneNumber());

            Branch savedBranch = branchRepository.save(branch);

            // Audit log for branch creation
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", savedBranch.getId());
            newData.put("name", savedBranch.getName());
            newData.put("address", savedBranch.getAddress());
            newData.put("phoneNumber", savedBranch.getPhoneNumber());

            auditLogService.logCreate("Branch", savedBranch.getId().toString(), newData);

            return convertToResponse(savedBranch);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public BranchResponseDTO updateBranch(Long id, UpdateBranchRequestDTO requestDTO) {
        // Get current user with multiple fallback strategies
        String currentUser = getCurrentUserWithMultipleStrategies();
        System.out.println("=== UPDATE BRANCH - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Branch existingBranch = branchRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

            // Store old data for audit log
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("id", existingBranch.getId());
            oldData.put("name", existingBranch.getName());
            oldData.put("address", existingBranch.getAddress());
            oldData.put("phoneNumber", existingBranch.getPhoneNumber());

            if (!existingBranch.getName().equals(requestDTO.getName()) &&
                    branchRepository.existsByName(requestDTO.getName())) {
                throw new RuntimeException("Branch name already exists: " + requestDTO.getName());
            }

            existingBranch.setName(requestDTO.getName());
            existingBranch.setAddress(requestDTO.getAddress());
            existingBranch.setPhoneNumber(requestDTO.getPhoneNumber());

            Branch updatedBranch = branchRepository.save(existingBranch);

            // Store new data for audit log
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", updatedBranch.getId());
            newData.put("name", updatedBranch.getName());
            newData.put("address", updatedBranch.getAddress());
            newData.put("phoneNumber", updatedBranch.getPhoneNumber());

            auditLogService.logUpdate("Branch", id.toString(), oldData, newData);

            return convertToResponse(updatedBranch);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        // Get current user with multiple fallback strategies
        String currentUser = getCurrentUserWithMultipleStrategies();
        System.out.println("=== DELETE BRANCH - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Branch branch = branchRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

            // Store data for audit log before deletion
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("id", branch.getId());
            oldData.put("name", branch.getName());
            oldData.put("address", branch.getAddress());
            oldData.put("phoneNumber", branch.getPhoneNumber());

            branchRepository.deleteById(id);

            auditLogService.logDelete("Branch", id.toString(), oldData);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }

    @Override
    public List<BranchResponseDTO> getBranchesForCurrentUser() {
        try {
            String username = getCurrentUsernameWithMultipleStrategies();
            System.out.println("=== getBranchesForCurrentUser - Username: " + username + " ===");

            if (username == null || "System".equals(username) || "anonymousUser".equals(username)) {
                return List.of();
            }

            // Try to find user by email first
            Optional<User> userOptional = userRepository.findByEmail(username);

            if (userOptional.isEmpty()) {
                // Try to find by fullName as fallback
                Optional<User> userByFullName = userRepository.findByFullName(username);
                if (userByFullName.isPresent()) {
                    User user = userByFullName.get();
                    return getUserBranches(user);
                } else {
                    throw new RuntimeException("User not found: " + username);
                }
            }

            User user = userOptional.get();
            return getUserBranches(user);

        } catch (Exception e) {
            System.err.println("Error in getBranchesForCurrentUser: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private List<BranchResponseDTO> getUserBranches(User user) {
        // Check if user has tenant role
        boolean isTenant = user.getRoles().stream()
                .anyMatch(role -> "TENANT".equalsIgnoreCase(role.getName()));

        if (isTenant) {
            // Return only branches assigned to this tenant
            return user.getBranches().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } else {
            // Return all branches for admin/manager users
            return branchRepository.findAll().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
    }

    private BranchResponseDTO convertToResponse(Branch branch) {
        BranchResponseDTO response = new BranchResponseDTO();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());
        response.setPhoneNumber(branch.getPhoneNumber());
        response.setCreatedAt(branch.getCreatedAt());
        response.setUpdatedAt(branch.getUpdatedAt());
        return response;
    }

    /**
     * MULTIPLE STRATEGIES TO GET CURRENT USER
     */
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

    private String getCurrentUsernameWithMultipleStrategies() {
        // Strategy 1: Try Security Context first
        String usernameFromSecurity = getCurrentUsernameFromSecurityContext();
        if (isValidUsername(usernameFromSecurity)) {
            return usernameFromSecurity;
        }

        // Strategy 2: Try JWT Token from Authorization header
        String usernameFromJwt = getCurrentUsernameFromJwtToken();
        if (isValidUsername(usernameFromJwt)) {
            return usernameFromJwt;
        }

        return null;
    }

    /**
     * Strategy 1: Security Context
     */
    private String getCurrentUserFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("=== SECURITY CONTEXT STRATEGY ===");
            System.out.println("Authentication: " + authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                System.out.println("Principal: " + principal);

                String username = extractUsernameFromPrincipal(principal);
                System.out.println("Extracted username: " + username);

                if (isValidUsername(username)) {
                    Optional<User> user = userRepository.findByEmail(username);
                    if (user.isPresent()) {
                        String fullName = user.get().getFullName();
                        System.out.println("Found user full name: " + fullName);
                        return (fullName != null && !fullName.trim().isEmpty()) ? fullName : username;
                    }
                    return username;
                }
            }

            System.out.println("Security Context: No authenticated user found");
            return "System";

        } catch (Exception e) {
            System.err.println("Error in Security Context strategy: " + e.getMessage());
            return "System";
        }
    }

    private String getCurrentUsernameFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                return extractUsernameFromPrincipal(principal);
            }
        } catch (Exception e) {
            System.err.println("Error getting username from SecurityContext: " + e.getMessage());
        }
        return null;
    }

    /**
     * Strategy 2: JWT Token from Authorization Header
     */
    private String getCurrentUserFromJwtToken() {
        try {
            String token = extractTokenFromRequest();
            if (token != null && !token.trim().isEmpty()) {
                System.out.println("=== JWT TOKEN STRATEGY ===");
                System.out.println("Token found, length: " + token.length());

                Claims claims = tokenProvider.getClaimsFromToken(token);
                String email = claims.get(JwtConstants.EMAIL, String.class);

                System.out.println("Email from JWT: " + email);

                if (email != null && !email.trim().isEmpty()) {
                    Optional<User> user = userRepository.findByEmail(email);
                    if (user.isPresent()) {
                        String fullName = user.get().getFullName();
                        System.out.println("JWT User found: " + fullName + " (" + email + ")");
                        return fullName != null && !fullName.trim().isEmpty() ? fullName : email;
                    } else {
                        System.out.println("JWT User not found for email: " + email);
                        return email;
                    }
                }
            } else {
                System.out.println("JWT Strategy: No token found in request");
            }
        } catch (Exception e) {
            System.err.println("JWT Strategy Error: " + e.getMessage());
        }
        return "System";
    }

    private String getCurrentUsernameFromJwtToken() {
        try {
            String token = extractTokenFromRequest();
            if (token != null && !token.trim().isEmpty()) {
                Claims claims = tokenProvider.getClaimsFromToken(token);
                return claims.get(JwtConstants.EMAIL, String.class);
            }
        } catch (Exception e) {
            System.err.println("Error extracting username from JWT: " + e.getMessage());
        }
        return null;
    }

    /**
     * Strategy 3: Custom Header (for testing)
     */
    private String getCurrentUserFromCustomHeader() {
        try {
            String testUser = request.getHeader("X-Test-User");
            if (testUser != null && !testUser.trim().isEmpty()) {
                System.out.println("=== CUSTOM HEADER STRATEGY ===");
                System.out.println("Found test user from header: " + testUser);
                return testUser;
            }
        } catch (Exception e) {
            System.err.println("Error in custom header strategy: " + e.getMessage());
        }
        return "System";
    }

    /**
     * Helper Methods
     */
    private String extractTokenFromRequest() {
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("Authorization Header: " + (authHeader != null ? authHeader : "Null"));

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                System.out.println("Extracted token length: " + token.length());
                return token;
            } else {
                System.out.println("No Bearer token found in header");
            }
        } catch (Exception e) {
            System.err.println("Error extracting token from request: " + e.getMessage());
        }
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

    private boolean isValidUsername(String username) {
        return username != null &&
                !username.trim().isEmpty() &&
                !"anonymousUser".equals(username) &&
                !"system".equalsIgnoreCase(username);
    }
}