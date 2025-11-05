package com.spring.Controller;

import com.spring.DTO.response.ApiResponse;
import com.spring.DTO.response.BranchResponseDTO;
import com.spring.DTO.request.CreateBranchRequestDTO;
import com.spring.DTO.request.UpdateBranchRequestDTO;
import com.spring.Services.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    // ✅ Change here
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> createBranch(
            @Valid @RequestBody CreateBranchRequestDTO request) {
        BranchResponseDTO createdBranch = branchService.createBranch(request);
        return new ResponseEntity<>(
                ApiResponse.created("Branch created successfully", createdBranch),
                HttpStatus.CREATED
        );
    }

    // ✅ Change here
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequestDTO request) {
        BranchResponseDTO updatedBranch = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updatedBranch));
    }

    // ✅ Change here
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully"));
    }
}
