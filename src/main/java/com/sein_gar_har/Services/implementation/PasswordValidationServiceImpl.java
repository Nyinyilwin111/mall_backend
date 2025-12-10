package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.Services.PasswordValidationService;
import com.sein_gar_har.dto.request.ChangePasswordRequest;
import com.sein_gar_har.dto.response.PasswordValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PasswordValidationServiceImpl implements PasswordValidationService {

    @Value("${app.password.min-length:8}")
    private int minLength;

    @Value("${app.password.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${app.password.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${app.password.require-numbers:true}")
    private boolean requireNumbers;

    @Value("${app.password.require-special:false}")
    private boolean requireSpecial;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private static final Pattern[] WEAK_PATTERNS = {
            Pattern.compile("^(.)\\1+$"),
            Pattern.compile("^12345678"),
            Pattern.compile("^password", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^qwerty", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^abcdef", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^admin", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public PasswordValidationResult validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null) {
            errors.add("Password cannot be null");
            return PasswordValidationResult.invalid("Invalid password", errors);
        }

        // Check length
        if (password.length() < minLength) {
            errors.add(String.format("Password must be at least %d characters long", minLength));
        }

        if (password.length() > 128) {
            errors.add("Password must not exceed 128 characters");
        }

        // Check character requirements
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter (A-Z)");
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter (a-z)");
        }

        if (requireNumbers && !NUMBER_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one number (0-9)");
        }

        if (requireSpecial && !SPECIAL_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one special character (!@#$%^&* etc.)");
        }

        // Check weak patterns
        for (Pattern pattern : WEAK_PATTERNS) {
            if (pattern.matcher(password).find()) {
                errors.add("Password is too common and easily guessable");
                break;
            }
        }

        if (errors.isEmpty()) {
            return PasswordValidationResult.valid("Password is valid");
        } else {
            return PasswordValidationResult.invalid(errors.get(0), errors);
        }
    }

    @Override
    public PasswordValidationResult validatePasswordChange(ChangePasswordRequest request) {
        List<String> errors = new ArrayList<>();

        // Validate new password
        PasswordValidationResult passwordValidation = validatePassword(request.getNewPassword());
        if (!passwordValidation.isValid()) {
            errors.addAll(passwordValidation.getErrors());
        }

        // Check if new password is same as current
        if (request.getCurrentPassword() != null &&
                request.getNewPassword() != null &&
                request.getCurrentPassword().equals(request.getNewPassword())) {
            errors.add("New password must be different from current password");
        }

        if (errors.isEmpty()) {
            return PasswordValidationResult.valid("Password change request is valid");
        } else {
            return PasswordValidationResult.invalid(errors.get(0), errors);
        }
    }
}