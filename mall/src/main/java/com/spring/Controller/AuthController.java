package com.spring.Controller;

import com.spring.DTO.response.JwtResponse;
import com.spring.DTO.request.LoginRequest;
import com.spring.DTO.request.SignupRequest;
import com.spring.Entity.User;
import com.spring.Repository.UserRepository;
import com.spring.Services.UserService;
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
    UserService userService;
//    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for user: " + loginRequest.getFullName());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getFullName(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(loginRequest.getFullName());

            User user = userService.findByUsername(loginRequest.getFullName());

            System.out.println("Login successful for user: " + loginRequest.getFullName());
            return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getFullName(), user.getEmail(), user.getRoles()));

        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials for user: " + loginRequest.getFullName());
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
            System.out.println("Signup attempt for user: " + signUpRequest.getFullName());

            if (userService.existsByUsername(signUpRequest.getFullName())) {
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }

            if (userService.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            User user = new User();
            user.setFullName(signUpRequest.getFullName());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setEnabled(true); // Make sure this is set

            userService.save(user);
            System.out.println("User registered successfully: " + signUpRequest.getFullName());

            return ResponseEntity.ok(new JwtResponse(null, user.getId(), user.getFullName(), user.getEmail(), user.getRoles()));

        } catch (Exception e) {
            System.out.println("Signup error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error during registration: " + e.getMessage());
        }
    }
}