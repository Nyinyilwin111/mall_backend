package com.sein_gar_har.controller;

import com.sein_gar_har.Services.OtpService;
import com.sein_gar_har.dto.request.ForgotPasswordRequestDTO;
import com.sein_gar_har.dto.request.VerifyOtpRequestDTO;
import com.sein_gar_har.dto.response.ApiResponse;
import com.sein_gar_har.exception.UserException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    OtpService otpService;

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