package com.sein_gar_har.controller;

import com.sein_gar_har.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/generate-token")
    public String generateTestToken() {
        try {
            String token = jwtUtil.generateToken("testuser");
            System.out.println("Generated token: " + token);

            // Test extraction
            String username = jwtUtil.extractUsername(token);
            System.out.println("Extracted username: " + username);

            // Test validation
            boolean isValid = jwtUtil.validateToken(token);
            System.out.println("Token valid: " + isValid);

            return "Token: " + token + "\nUsername: " + username + "\nValid: " + isValid;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    @GetMapping("/connection")
    public String testConnection() {
        return "✅ Backend is running successfully on port 8081!";
    }

    @GetMapping("/cors-test")
    public String testCors() {
        return "✅ CORS is working properly!";
    }
}