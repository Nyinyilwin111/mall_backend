package com.sein_gar_har.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequestDTO(
        @NotBlank String email,
        @NotBlank String otp,
        @NotBlank String newPassword
) {}