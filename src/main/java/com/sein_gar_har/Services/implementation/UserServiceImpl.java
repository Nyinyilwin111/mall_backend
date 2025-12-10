package com.sein_gar_har.Services.implementation;

import com.google.api.pathtemplate.ValidationException;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.PasswordEncryptionService;
import com.sein_gar_har.Services.PasswordValidationService;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.ChangePasswordRequest;
import com.sein_gar_har.dto.request.UpdateUserRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.dto.response.ChangePasswordResponse;
import com.sein_gar_har.dto.response.PasswordValidationResult;
import com.sein_gar_har.dto.response.UserResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.ResourceNotFoundException;
import com.sein_gar_har.exception.UserException;
import com.sein_gar_har.Services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    private final TokenProvider tokenProvider;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidationService passwordValidationService;
    private final PasswordEncryptionService passwordEncryptionService;

    @Override
    public User findUserById(UUID id) throws UserException {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return user.get();
        }

        throw new UserException("User not found with id " + id);
    }

    @Override
    public User findUserByProfile(String jwt) throws UserException {
        String email = String.valueOf(tokenProvider.getClaimsFromToken(jwt).get(JwtConstants.EMAIL));

        if (email == null) {
            throw new BadCredentialsException("Invalid token");
        }

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return user.get();
        }

        throw new UserException("User not found with email " + email);
    }

    @Override
    @Transactional
    public User updateUser(UUID id, UpdateUserRequestDTO request) throws UserException {
        User user = findUserById(id);

        // Store old data for audit log
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("id", user.getId());
        oldData.put("fullName", user.getFullName());
        oldData.put("email", user.getEmail());
        oldData.put("enabled", user.isEnabled());
        oldData.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));

        boolean hasChanges = false;

        if (Objects.nonNull(request.fullName()) && !request.fullName().equals(user.getFullName())) {
            user.setFullName(request.fullName());
            hasChanges = true;
        }

        User updatedUser = userRepository.save(user);

        if (hasChanges) {
            // Store new data for audit log
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", updatedUser.getId());
            newData.put("fullName", updatedUser.getFullName());
            newData.put("email", updatedUser.getEmail());
            newData.put("enabled", updatedUser.isEnabled());
            newData.put("roles", updatedUser.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet()));

            // Audit log for user update
            auditLogService.logUpdate(
                    "User",
                    id.toString(),
                    oldData,
                    newData
            );
        }

        return updatedUser;
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.findByFullNameOrEmail(query).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

    @Override
    public List<User> searchUserByName(String name) {
        return userRepository.findByName(name).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByFullName(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // In UserServiceImpl.java - Simple alternative
    @Override
    @Transactional
    public void save(User user) {
        boolean isNewUser = user.getId() == null;

        User savedUser = userRepository.save(user);

        if (isNewUser) {
            // Store user data for audit log
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", savedUser.getId());
            newData.put("fullName", savedUser.getFullName());
            newData.put("email", savedUser.getEmail());
            newData.put("enabled", savedUser.isEnabled());

            // Use a temporary method that forces "System" as performer
            // We'll handle this in the AuditLogService by checking the context
            auditLogService.logCreate(
                    "User",
                    savedUser.getId().toString(),
                    newData
            );
        }
    }
    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByFullName(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return optionalUser.get();
    }

    @Override
    public List<User> getAllUsersWithRoles() {
        return userRepository.findAllWithRoles();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserResponseDTO getCurrentUserInfo(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmailWithRolesAndBranches(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return convertToUserResponseDTO(user);
    }

    // Helper method to delete user with audit log
    @Transactional
    public void deleteUser(UUID id) throws UserException {
        User user = findUserById(id);

        // Store data for audit log before deletion
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("id", user.getId());
        oldData.put("fullName", user.getFullName());
        oldData.put("email", user.getEmail());
        oldData.put("enabled", user.isEnabled());
        oldData.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));

        userRepository.delete(user);

        // Audit log for user deletion
        auditLogService.logDelete(
                "User",
                id.toString(),
                oldData
        );
    }

    // Helper method to enable/disable user with audit log
    @Transactional
    public User toggleUserStatus(UUID id, boolean enabled) throws UserException {
        User user = findUserById(id);

        Map<String, Object> oldData = new HashMap<>();
        oldData.put("fullName", user.getFullName());
        oldData.put("email", user.getEmail());
        oldData.put("enabled", user.isEnabled());

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        Map<String, Object> newData = new HashMap<>();
        newData.put("fullName", updatedUser.getFullName());
        newData.put("email", updatedUser.getEmail());
        newData.put("enabled", updatedUser.isEnabled());

        auditLogService.logUpdate(
                "User",
                id.toString(),
                oldData,
                newData
        );

        return updatedUser;
    }

    @Override
    public List<User> findByRole(String role) {
        return userRepository.  findUsersByRole(role);
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

    @Override
    @Transactional
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        UUID userId = request.getUserId();

        try {

            // Validate request
            validateChangePasswordRequest(request);

            // Get user
            User user = findUserById(userId);

            // Decrypt passwords if encrypted
            String currentPassword = decryptPassword(request.getCurrentPassword(), request.getEncryption());
            String newPassword = decryptPassword(request.getNewPassword(), request.getEncryption());

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {

                return ChangePasswordResponse.error("Current password is incorrect");
            }

            // Validate new password
            PasswordValidationResult validation = passwordValidationService
                    .validatePasswordChange(new ChangePasswordRequest(
                            userId,
                            currentPassword,
                            newPassword,
                            request.getEncryption()
                    ));

            if (!validation.isValid()) {

                return ChangePasswordResponse.error(validation.getMessage());
            }

            // Update password
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            userRepository.save(user);

            return ChangePasswordResponse.success("Password changed successfully");

        } catch (ResourceNotFoundException e) {
            return ChangePasswordResponse.error("User not found");
        } catch (ValidationException e) {
            return ChangePasswordResponse.error(e.getMessage());
        } catch (Exception e) {
            return ChangePasswordResponse.error("Failed to change password. Please try again.");
        }
    }

    private void validateChangePasswordRequest(ChangePasswordRequest request) {
        if (request.getUserId() == null) {
            throw new ValidationException("User ID is required");
        }

        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            throw new ValidationException("Current password is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new ValidationException("New password is required");
        }

        // Validate encryption if provided
        if (request.getEncryption() != null) {
            if (!passwordEncryptionService.verifyEncryption(
                    request.getCurrentPassword(),
                    request.getEncryption().getMethod())) {
                throw new ValidationException("Invalid encrypted data");
            }
        }
    }

    private String decryptPassword(String encryptedPassword,
                                   ChangePasswordRequest.EncryptionMetadata encryption) {
        // If no encryption metadata, assume plain text
        if (encryption == null || encryption.getMethod() == null) {
            return encryptedPassword;
        }

        if ("AES-256-CBC".equals(encryption.getMethod()) || "AES".equals(encryption.getMethod())) {
            if (encryption.getIv() == null) {
                throw new ValidationException("IV is required for AES decryption");
            }
            return passwordEncryptionService.decryptAES(encryptedPassword, encryption.getIv());
        }
        return encryptedPassword;
    }
}