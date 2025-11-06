package com.spring.Controller;

import com.spring.DTO.request.UserBranchRequestDTO;
import com.spring.DTO.response.UserResponseDTO;
import com.spring.Services.UserBranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-branches")
@RequiredArgsConstructor
public class UserBranchController {

    private final UserBranchService userBranchService;

    @PostMapping("/assign")
    public ResponseEntity<UserResponseDTO> assignBranchesToUser(
            @Valid @RequestBody UserBranchRequestDTO userBranchRequestDTO) {
        UserResponseDTO response = userBranchService.assignBranchesToUser(userBranchRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    public ResponseEntity<UserResponseDTO> removeBranchesFromUser(
            @Valid @RequestBody UserBranchRequestDTO userBranchRequestDTO) {
        UserResponseDTO response = userBranchService.removeBranchesFromUser(userBranchRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUserBranches(
            @Valid @RequestBody UserBranchRequestDTO userBranchRequestDTO) {
        UserResponseDTO response = userBranchService.updateUserBranches(userBranchRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponseDTO> getUserWithBranches(@PathVariable UUID userId) {
        UserResponseDTO response = userBranchService.getUserWithBranches(userId);
        return ResponseEntity.ok(response);
    }
}