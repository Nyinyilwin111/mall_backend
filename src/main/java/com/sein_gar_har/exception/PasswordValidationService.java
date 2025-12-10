package com.sein_gar_har.exception;


import com.sein_gar_har.dto.request.ChangePasswordRequest;
import com.sein_gar_har.dto.response.PasswordValidationResult;

public interface PasswordValidationService {

    PasswordValidationResult validatePassword(String password);

    PasswordValidationResult validatePasswordChange(ChangePasswordRequest request);
}
