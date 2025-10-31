package com.spring.Controller;

import com.spring.Entity.Branch;
import com.spring.Services.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class BranchController {
    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getBranchById(@PathVariable Long id) {
        try {
            Branch branch = branchService.getBranchById(id);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        // Add validation
        if (branch.getName() == null || branch.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (branch.getAddress() == null || branch.getAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Branch savedBranch = branchService.createBranch(branch);
            return ResponseEntity.ok(savedBranch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable Long id, @RequestBody Branch branch) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branch);
            return ResponseEntity.ok(updatedBranch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        try {
            branchService.deleteBranch(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Branch>> getUserBranches(@PathVariable String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            List<Branch> userBranches = branchService.getUserBranches(uuid);
            return ResponseEntity.ok(userBranches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // SEARCH ENDPOINT
    @GetMapping("/search")
    public ResponseEntity<List<Branch>> searchBranches(@RequestParam String name) {
        try {
            List<Branch> branches = branchService.searchBranchesByName(name);
            return ResponseEntity.ok(branches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{branchId}/assign-user/{userId}")
    public ResponseEntity<Void> assignUserToBranch(
            @PathVariable Long branchId,
            @PathVariable UUID userId,
            @RequestParam String role) {
        try {
            branchService.assignUserToBranch(userId, branchId, role);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}