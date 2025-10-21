package com.spring.Controller;

import com.spring.DTO.response.JwtResponse;
import com.spring.DTO.request.LoginRequest;
import com.spring.DTO.request.SignupRequest;
import com.spring.Entity.User;
import com.spring.Repository.UserRepository;
import com.spring.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for user: " + loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(loginRequest.getUsername());

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("Login successful for user: " + loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRoles()));

        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials for user: " + loginRequest.getUsername());
            return ResponseEntity.badRequest().body("Error: Invalid username or password");
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: Authentication failed - " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {
            System.out.println("Signup attempt for user: " + signUpRequest.getUsername());

            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setEnabled(true); // Make sure this is set

            userRepository.save(user);
            System.out.println("User registered successfully: " + signUpRequest.getUsername());

            return ResponseEntity.ok("User registered successfully!");

        } catch (Exception e) {
            System.out.println("Signup error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error during registration: " + e.getMessage());
        }
    }
}