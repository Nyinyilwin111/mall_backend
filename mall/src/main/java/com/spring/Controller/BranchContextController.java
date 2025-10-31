package com.spring.Controller;
import com.spring.Services.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/branch-context")
@RequiredArgsConstructor
public class BranchContextController {
    private final BranchService branchService;

    @PostMapping("/select/{branchId}")
    public ResponseEntity<Map<String, String>> selectBranch(@PathVariable Long branchId, @RequestHeader("X-User-Id") UUID userId) {
        // Validate user has access to this branch
        branchService.getUserBranches(userId).stream()
                .filter(branch -> branch.getId().equals(branchId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User doesn't have access to this branch"));

        Map<String, String> response = new HashMap<>();
        response.put("message", "Branch selected successfully");
        response.put("branchId", branchId.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/branches")
    public ResponseEntity<?> getUserAccessibleBranches(@PathVariable UUID userId) {
        return ResponseEntity.ok(branchService.getUserBranches(userId));
    }
}