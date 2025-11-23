package com.sein_gar_har.controller;

import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.dto.request.CreateBranchRequestDTO;
import com.sein_gar_har.dto.request.UpdateBranchRequestDTO;
import com.sein_gar_har.dto.response.ApiResponse;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BranchResponseDTO>>> getAllBranches() {
        List<BranchResponseDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully", branches));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> getBranchById(@PathVariable Long id) {
        BranchResponseDTO branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Branch retrieved successfully", branch));
    }

    @GetMapping("/my-branches")
    public ResponseEntity<ApiResponse<List<BranchResponseDTO>>> getMyBranches(Principal principal) {
        List<BranchResponseDTO> branches = branchService.getBranchesForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully", branches));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> createBranch(
            @Valid @RequestBody CreateBranchRequestDTO request) {

        System.out.println("=== CONTROLLER CREATE BRANCH ===");

        BranchResponseDTO createdBranch = branchService.createBranch(request);
        return new ResponseEntity<>(
                ApiResponse.created("Branch created successfully", createdBranch),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequestDTO request) {
        BranchResponseDTO updatedBranch = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updatedBranch));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully"));
    }
}