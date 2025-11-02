package com.spring.Services;

import com.spring.Entity.Branch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchService {

    List<Branch> getAllBranches();

    Optional<Branch> getBranchById(UUID id);

    Branch createBranch(Branch branch);

    Branch updateBranch(UUID id, Branch branchDetails);

    void deleteBranch(UUID id);

    // User-related operations
    List<Branch> getUserBranches(UUID userId);

    // Simple search operation
    List<Branch> searchBranchesByName(String name);
}
