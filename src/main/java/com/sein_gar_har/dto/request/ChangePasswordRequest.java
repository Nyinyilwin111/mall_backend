package com.sein_gar_har.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotNull(message = "User ID is required")
    private UUID userId; // Use UUID type but with @NotNull instead of @NotBlank

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    private EncryptionMetadata encryption;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncryptionMetadata {
        private String method;
        private String version;
        private String timestamp;
        private String iv;
    }
}