package com.spring.Controller;

import com.spring.Entity.Branch;
import com.spring.Services.BranchService;
import com.spring.Services.UserBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    @Autowired
    BranchService branchService;

    @Autowired
    UserBranchService userBranchService;

    // GET all branches
    @GetMapping("/getAll")
    public List<Branch> getAllBranches() {

        return branchService.getAllBranches();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Branch>> getBranchById(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Optional<Branch> branch = branchService.getBranchById(uuid);
            return ResponseEntity.ok(branch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
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

    @PutMapping("/update/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable UUID id, @RequestBody Branch branch) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branch);
            return ResponseEntity.ok(updatedBranch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable UUID id) {
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
            @PathVariable UUID branchId,
            @PathVariable UUID userId,
            @RequestParam String role) {
        try {
            userBranchService.assignUserToBranch(userId, branchId, role);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
