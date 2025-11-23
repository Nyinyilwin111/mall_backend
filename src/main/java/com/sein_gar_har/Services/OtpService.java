package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.ForgotPasswordRequestDTO;
import com.sein_gar_har.dto.request.VerifyOtpRequestDTO;
import com.sein_gar_har.exception.UserException;

public interface OtpService {
    void generateOtp(ForgotPasswordRequestDTO request) throws UserException;
    void verifyOtpAndResetPassword(VerifyOtpRequestDTO request) throws UserException;

}