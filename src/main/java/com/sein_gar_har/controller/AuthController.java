package com.sein_gar_har.controller;

import com.sein_gar_har.RepositoryMain.RoleRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.Services.implementation.CustomUserDetailsService;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.ChangePasswordRequest;
import com.sein_gar_har.dto.request.LoginRequestDTO;
import com.sein_gar_har.dto.request.SignupRequest;
import com.sein_gar_har.dto.request.UpdateUserRequestDTO;
import com.sein_gar_har.dto.response.*;
import com.sein_gar_har.entity.Role;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponseDTO> signup(@RequestBody SignupRequest signupRequestDTO) throws UserException {

        final String email = signupRequestDTO.getEmail();
        final String password = signupRequestDTO.getPassword();
        final String fullName = signupRequestDTO.getFullName();

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            throw new UserException("Account with email " + email + " already exists");
        }

//        User user = new User();
//        user.setFullName(signupRequestDTO.getFullName());
//        user.setEmail(signupRequestDTO.getEmail());
//        user.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));
//        user.setEnabled(true);

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .enabled(true)
                .build();


        // ✅ Assign default role (now roleRepository is properly autowired)
        Role guestRole = roleRepository.findByName("GUEST")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("GUEST");
                    return roleRepository.save(newRole);
                });
        newUser.getRoles().add(guestRole);

        userService.save(newUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .token(jwt)
                .isAuthenticated(true)
                .build();

        log.info("User {} successfully signed up", email);

        return new ResponseEntity<>(loginResponseDTO, HttpStatus.ACCEPTED);
    }

    public Authentication authenticateReq(String username, String password) {

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }


    @PostMapping("/signin")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {

        final String fullName = loginRequestDTO.fullName();
        final String password = loginRequestDTO.password();

        // Authenticate user
        Authentication authentication = authenticateReq(fullName, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Fetch full User entity by fullName
        User user = userService.findByUsername(fullName); // make sure this method exists

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication); // pass username/email depending on your JWT setup

        // Build response
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .id(user.getId())
                .token(jwt)
                .isAuthenticated(true)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(
                        user.getRoles().stream()
                                .map(RoleResponseDTO::new)
                                .collect(Collectors.toSet())
                )
                .branches(
                        user.getBranches() != null
                                ? user.getBranches().stream()
                                .map(b -> new BranchResponseDTO(
                                        b.getId(),
                                        b.getName(),
                                        b.getAddress(),
                                        b.getPhoneNumber(),
                                        b.getCreatedAt(),
                                        b.getUpdatedAt()
                                ))
                                .collect(Collectors.toSet())
                                : Set.of()
                )
                .build();

        log.info("User {} successfully signed in", fullName);

<<<<<<< HEAD
    return new ResponseEntity<>(loginResponseDTO, HttpStatus.ACCEPTED);
}
=======
        return new ResponseEntity<>(loginResponseDTO, HttpStatus.ACCEPTED);
    }
>>>>>>> 7849d7fed339778c291f082b6b5ba33d53d04c80
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        try {
            // Process password change
            ChangePasswordResponse response = userService.changePassword(request);

            // Return appropriate response based on success
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(
                        response.getMessage(),
                        response
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(response.getMessage(), HttpStatus.BAD_REQUEST.value()));
            }

        } catch (Exception e) {
            log.error("Error processing password change: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Internal server error. Please try again later.",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @GetMapping("/password-strength")
    public ResponseEntity<ApiResponse<String>> checkPasswordStrength(
            @RequestParam String password) {
        try {
            // Simple strength check for frontend
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Password is empty",
                        "EMPTY"
                ));
            }

            int length = password.length();
            boolean hasUpper = password.matches(".*[A-Z].*");
            boolean hasLower = password.matches(".*[a-z].*");
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

            int score = 0;
            if (hasUpper) score++;
            if (hasLower) score++;
            if (hasDigit) score++;
            if (hasSpecial) score++;
            if (length >= 8) score++;
            if (length >= 12) score++;

            String strength;
            if (score >= 5) strength = "VERY_STRONG";
            else if (score >= 4) strength = "STRONG";
            else if (score >= 3) strength = "MEDIUM";
            else if (score >= 2) strength = "WEAK";
            else strength = "VERY_WEAK";

            return ResponseEntity.ok(ApiResponse.success(
                    "Password strength checked",
                    strength
            ));

        } catch (Exception e) {
            log.error("Error checking password strength: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check password strength",HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

}
