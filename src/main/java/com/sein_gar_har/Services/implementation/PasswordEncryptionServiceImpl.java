package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.Services.PasswordEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    @Value("${app.encryption.aes-key:your-default-key-32-bytes-long-1234567890123456}")
    private String aesKey;

    @Override
    public String decryptAES(String encryptedData, String iv) {
        try {
            if (encryptedData == null || iv == null) {
                throw new IllegalArgumentException("Encrypted data and IV are required");
            }

            // Decode base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);

            // Ensure key is 32 bytes for AES-256
            byte[] key = new byte[32];
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

            // Ensure IV is 16 bytes
            byte[] ivParam = new byte[16];
            System.arraycopy(ivBytes, 0, ivParam, 0, Math.min(ivBytes.length, 16));

            // Create cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParam);

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("AES decryption failed: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to decrypt password", e);
        }
    }

    @Override
    public boolean verifyEncryption(String encryptedData, String method) {
        if (encryptedData == null || method == null) {
            return false;
        }

        try {
            // Try to decode as base64
            Base64.getDecoder().decode(encryptedData);

            // Validate method
            return "AES-256-CBC".equals(method) || "AES".equals(method);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}