package com.sein_gar_har.controller;

import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.dto.request.UpdateAvatarRequest;
import com.sein_gar_har.dto.request.UpdateUserRequestDTO;
import com.sein_gar_har.dto.response.ApiResponse;
import com.sein_gar_har.dto.response.ApiResponseDTO;
import com.sein_gar_har.dto.response.UserDTO;
import com.sein_gar_har.dto.response.UserResponseDTO;
import com.sein_gar_har.entity.Role;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.UserException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@RequestHeader(JwtConstants.TOKEN_HEADER) String token) throws UserException {

        // remove "Bearer " if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("token ---- = "+token);
        }
        User user = userService.findUserByProfile(token);

        return new ResponseEntity<>(UserDTO.fromUser(user), HttpStatus.OK);
    }

    @GetMapping("/profile/webp")
    public ResponseEntity<UserDTO> getUserProfileForWebsocket(@RequestHeader(JwtConstants.TOKEN_HEADER) String token) throws UserException {

        User user = userService.findUserByProfile(token);

        return new ResponseEntity<>(UserDTO.fromUser(user), HttpStatus.OK);
    }

    @GetMapping("/{query}")
    public ResponseEntity<List<UserDTO>> searchUsers(@PathVariable String query) {

        List<User> users = userService.searchUser(query);

        return new ResponseEntity<>(UserDTO.fromUsersAsList(users), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Set<UserDTO>> searchUsersByName(@RequestParam("name") String name) {

        List<User> users = userService.searchUserByName(name);

        return new ResponseEntity<>(UserDTO.fromUsers(users), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponseDTO> updateUser(@RequestBody UpdateUserRequestDTO request,
                                                     @RequestHeader(JwtConstants.TOKEN_HEADER) String token)
            throws UserException {

        User user = userService.findUserByProfile(token);
        user = userService.updateUser(user.getId(), request);
        log.info("User updated: {}", user.getEmail());

        ApiResponseDTO response = ApiResponseDTO.builder()
                .message("User updated")
                .status(true)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // Add this to your UserController.java
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsersWithRoles(); // Use the new method
        return new ResponseEntity<>(UserDTO.fromUsersAsList(users), HttpStatus.OK);
    }

    @GetMapping("/debug-all-users")
    public ResponseEntity<List<Map<String, Object>>> debugAllUsers() {
        List<User> users = userService.findAll();
        List<Map<String, Object>> userInfo = users.stream()
                .map(user -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", user.getId());
                    info.put("email", user.getEmail());
                    info.put("fullName", user.getFullName());
                    info.put("enabled", user.isEnabled());
                    info.put("roles", user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toList()));
                    info.put("branches", user.getBranches().stream()
                            .map(branch -> branch.getName())
                            .collect(Collectors.toList()));
                    return info;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUserInfo(Principal principal) {
        try {
            UserResponseDTO userInfo = userService.getCurrentUserInfo(principal);
            return ResponseEntity.ok(ApiResponse.success("User info retrieved successfully", userInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user info: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    // UserController.java - Add this method
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<User> toggleUserStatus(@PathVariable UUID id, @RequestParam boolean enabled) {
        try {
            User user = userService.toggleUserStatus(id, enabled);
            return ResponseEntity.ok(user);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}/roles")       // this is new add by nyinyilwin
    public ResponseEntity<List<String>> getUserRoles(@PathVariable UUID userId) {
        try {
            User user = userService.findUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();

            System.out.println("📋 Fetching roles for user " + userId + ": " + roleNames);
            return ResponseEntity.ok(roleNames);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update-avatar")
    public ResponseEntity<ApiResponseDTO> updateAvatar(
            @Valid @RequestBody UpdateAvatarRequest request,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String token)
            throws UserException {

        try {
            log.info("=== Received avatar update request ===");
            log.info("User ID from request: {}", request.getUserId());
            log.info("Avatar URL: {}", request.getAvatarUrl());
            log.info("Token received: {}", token != null ? "Yes" : "No");

            // 1. Get authenticated user from token
            User authenticatedUser = userService.findUserByProfile(token);
            log.info("Authenticated user: {} (ID: {})",
                    authenticatedUser.getEmail(), authenticatedUser.getId());

            // 2. Parse requested user ID
            UUID requestedUserId;
            try {
                requestedUserId = UUID.fromString(request.getUserId());
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format: {}", request.getUserId());
                throw new UserException("Invalid user ID format");
            }

            // 3. Verify user is updating their own avatar
            if (!authenticatedUser.getId().equals(requestedUserId)) {
                log.warn("User {} attempted to update avatar for user {}",
                        authenticatedUser.getId(), requestedUserId);
                throw new UserException("You can only update your own avatar");
            }

            // 4. Find the user and update avatar URL
            User user = userService.findUserById(requestedUserId);
            String previousAvatarUrl = user.getAvatarUrl();
            user.setAvatarUrl(request.getAvatarUrl());
            userService.save(user);

            log.info("✅ Avatar updated successfully for user: {}", user.getEmail());
            log.info("Previous avatar: {}, New avatar: {}",
                    previousAvatarUrl, request.getAvatarUrl());

            // 5. Create response - Include details in message since no data field
            String successMessage = String.format(
                    "Avatar updated successfully. User: %s (%s), Avatar URL: %s",
                    user.getFullName(),
                    user.getEmail(),
                    request.getAvatarUrl()
            );

            ApiResponseDTO response = ApiResponseDTO.builder()
                    .message(successMessage)
                    .status(true)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (UserException e) {
            log.error("User error updating avatar: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error updating avatar: {}", e.getMessage(), e);
            throw new UserException("Failed to update avatar: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Map<String, Object>> getUserAvatar(
            @PathVariable String userId,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String token)
            throws UserException {

        try {
            log.info("Getting avatar for user ID: {}", userId);

            // Validate token
            User authenticatedUser = userService.findUserByProfile(token);
            log.debug("Authenticated as: {}", authenticatedUser.getEmail());

            UUID userUuid = UUID.fromString(userId);
            User user = userService.findUserById(userUuid);

            // Get avatar URL
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl == null && user.getProfileImage() != null) {
                avatarUrl = user.getProfileImage().getImageUrl();
            }

            // Create structured response
            Map<String, Object> response = new HashMap<>();
            response.put("status", true);

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                response.put("message", "Avatar found for user " + user.getEmail());
                response.put("avatarUrl", avatarUrl);
                response.put("avatarExists", true);
            } else {
                response.put("message", "No avatar set for user " + user.getEmail());
                response.put("avatarUrl", null);
                response.put("avatarExists", false);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting user avatar: {}", e.getMessage(), e);
            throw new UserException("Failed to get user avatar: " + e.getMessage());
        }
    }

    @GetMapping("/me/avatar")
    public ResponseEntity<ApiResponseDTO> getMyAvatar(
            @RequestHeader(JwtConstants.TOKEN_HEADER) String token)
            throws UserException {

        try {
            log.info("Getting current user's avatar");

            User user = userService.findUserByProfile(token);

            // Get avatar URL
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl == null && user.getProfileImage() != null) {
                avatarUrl = user.getProfileImage().getImageUrl();
            }

            // Create response
            String message;
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                message = String.format("Your avatar URL: %s", avatarUrl);
            } else {
                message = "You don't have an avatar set yet";
            }

            ApiResponseDTO response = ApiResponseDTO.builder()
                    .message(message)
                    .status(true)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting current user avatar: {}", e.getMessage(), e);
            throw new UserException("Failed to get your avatar: " + e.getMessage());
        }
    }

    @PutMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String token)
            throws UserException, IOException {

        try {
            log.info("Uploading avatar file: {}", file.getOriginalFilename());

            // Get authenticated user
            User user = userService.findUserByProfile(token);
            log.info("Uploading for user: {}", user.getEmail());

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File is empty", HttpStatus.BAD_REQUEST.value()));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Only image files are allowed", HttpStatus.BAD_REQUEST.value()));
            }

            // Validate file size (max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File size exceeds 5MB limit", HttpStatus.BAD_REQUEST.value()));
            }

            // Create uploads directory if it doesn't exist
            String uploadDir = "uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = "avatar_" + user.getId() + "_" + System.currentTimeMillis() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create URL for the uploaded file
            String avatarUrl = "/" + uploadDir + uniqueFilename;

            // ✅ SAVE TO DATABASE: Update user's avatar URL
            String previousAvatarUrl = user.getAvatarUrl();
            user.setAvatarUrl(avatarUrl);
            userService.save(user);

            log.info("✅ Avatar uploaded and saved to database successfully");
            log.info("Avatar URL: {}", avatarUrl);
            log.info("File saved to: {}", filePath.toAbsolutePath());
            log.info("Previous avatar: {}, New avatar: {}", previousAvatarUrl, avatarUrl);

            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", avatarUrl);
            responseData.put("path", filePath.toString());
            responseData.put("filename", uniqueFilename);
            responseData.put("userId", user.getId().toString());
            responseData.put("userEmail", user.getEmail());
            responseData.put("fileSize", file.getSize());
            responseData.put("contentType", file.getContentType());
            responseData.put("previousAvatarUrl", previousAvatarUrl);

            // Return ApiResponse with data
            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    "Avatar uploaded successfully and saved to database",
                    responseData
            );

            return ResponseEntity.ok(response);

        } catch (UserException e) {
            log.error("User error in avatar upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("Error uploading avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload avatar: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
