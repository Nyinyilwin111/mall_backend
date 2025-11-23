package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.Services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Try to find user by email first
        Optional<User> optionalUser = userRepository.findByEmail(username);

        // If not found by email, try by fullName
        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByFullName(username);
        }

        if (optionalUser.isEmpty()) {
            // Audit log for failed login attempt
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("status", "FAILED");
            loginData.put("reason", "User not found");

            auditLogService.logAction(
                    "LOGIN_FAILED",
                    "User",
                    "N/A",
                    null,
                    loginData
            );

            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User user = optionalUser.get();

        // Check if user is enabled
        if (!user.isEnabled()) {
            // Audit log for failed login (disabled account)
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("status", "FAILED");
            loginData.put("reason", "Account disabled");
            loginData.put("userId", user.getId());
            loginData.put("fullName", user.getFullName());
            loginData.put("email", user.getEmail());

            auditLogService.logAction(
                    "LOGIN_FAILED",
                    "User",
                    user.getId().toString(),
                    null,
                    loginData
            );

            throw new UsernameNotFoundException("Account is disabled: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Audit log for successful login - Use the special login method
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("status", "SUCCESS");
        loginData.put("userId", user.getId());
        loginData.put("fullName", user.getFullName());
        loginData.put("email", user.getEmail());

        // Use the special login method that accepts fullName
        auditLogService.logLogin(user.getId().toString(), loginData, user.getFullName());

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}