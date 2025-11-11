package com.spring.Services;

import com.spring.DTO.request.ForgotPasswordRequestDTO;
import com.spring.DTO.request.VerifyOtpRequestDTO;
import com.spring.Exceptions.UserException;

public interface OtpService {
    void generateOtp(ForgotPasswordRequestDTO request) throws UserException;
    void verifyOtpAndResetPassword(VerifyOtpRequestDTO request) throws UserException;

}
