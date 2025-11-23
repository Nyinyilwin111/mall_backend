package com.sein_gar_har.controller;

import com.sein_gar_har.Services.UserBranchService;
import com.sein_gar_har.dto.request.UserBranchRequestDTO;
import com.sein_gar_har.dto.response.UserResponseDTO;
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