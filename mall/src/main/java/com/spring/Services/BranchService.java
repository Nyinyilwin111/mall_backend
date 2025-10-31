package com.spring.Services;

import com.spring.Entity.Branch;
import java.util.List;
import java.util.UUID;

public interface BranchService {
    // Core CRUD operations
    List<Branch> getAllBranches();
    Branch getBranchById(Long id);
    Branch createBranch(Branch branch);
    Branch updateBranch(Long id, Branch branchDetails);
    void deleteBranch(Long id);

    // User-related operations
    List<Branch> getUserBranches(UUID userId);
    void assignUserToBranch(UUID userId, Long branchId, String role);

    // Simple search operation
    List<Branch> searchBranchesByName(String name);
}