package com.sein_gar_har.Services;

public interface PasswordEncryptionService {

    String decryptAES(String encryptedData, String iv);
    boolean verifyEncryption(String encryptedData, String method);
}
