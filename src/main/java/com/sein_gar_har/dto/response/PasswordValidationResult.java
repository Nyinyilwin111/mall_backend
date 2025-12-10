package com.sein_gar_har.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordValidationResult {
    private boolean isValid;
    private String message;
    private List<String> errors;

    public static PasswordValidationResult valid(String message) {
        return new PasswordValidationResult(true, message, null);
    }

    public static PasswordValidationResult invalid(String message, List<String> errors) {
        return new PasswordValidationResult(false, message, errors);
    }
}