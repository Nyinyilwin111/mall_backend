package com.spring.Controller;

import com.spring.DTO.request.ForgotPasswordRequestDTO;
import com.spring.DTO.request.VerifyOtpRequestDTO;
import com.spring.DTO.response.ApiResponse;
import com.spring.Exceptions.UserException;
import com.spring.Services.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final OtpService otpService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) throws UserException {
        otpService.generateOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your email", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) throws UserException {
        otpService.verifyOtpAndResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }
}
