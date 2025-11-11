package com.spring.Services.ServiceImplements;

import com.spring.DTO.request.ForgotPasswordRequestDTO;
import com.spring.DTO.request.VerifyOtpRequestDTO;
import com.spring.Entity.PasswordResetOtp;
import com.spring.Entity.User;
import com.spring.Exceptions.UserException;
import com.spring.Repository.PasswordResetOtpRepository;
import com.spring.Services.EmailService;
import com.spring.Services.OtpService;
import com.spring.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Add this import

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional // Add class-level transactional
public class OtpServiceImpl implements OtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Add method-level transactional
    public void generateOtp(ForgotPasswordRequestDTO request) throws UserException {
        System.out.println("=== OTP Generation Debug ===");
        System.out.println("Request email: " + request.email());

        Optional<User> userOptional = userService.findByEmail(request.email());
        System.out.println("User found: " + userOptional.isPresent());

        if (userOptional.isEmpty()) {
            throw new UserException("No user found with this email");
        }

        User user = userOptional.get();

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Save or update OTP - this will be transactional
        otpRepository.deleteByEmail(request.email()); // This now works with @Transactional
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setEmail(request.email());
        otp.setOtp(otpCode);
        otp.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpRepository.save(otp);

        // Send OTP email
        emailService.sendOtpEmail(request.email(), otpCode);

        System.out.println("OTP sent to: " + request.email());
    }

    @Override
    @Transactional // Add method-level transactional
    public void verifyOtpAndResetPassword(VerifyOtpRequestDTO request) throws UserException {
        Optional<PasswordResetOtp> otpOpt = otpRepository.findByEmailAndOtp(request.email(), request.otp());
        if (otpOpt.isEmpty()) {
            throw new UserException("Invalid OTP");
        }

        PasswordResetOtp otp = otpOpt.get();

        if (otp.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new UserException("OTP has expired");
        }

        // Get user by email properly
        Optional<User> userOptional = userService.findByEmail(request.email());
        if (userOptional.isEmpty()) {
            throw new UserException("User not found");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.save(user);

        // Delete OTP after successful reset - this now works with @Transactional
        otpRepository.delete(otp);

        System.out.println("Password reset successfully for: " + request.email());
    }
}