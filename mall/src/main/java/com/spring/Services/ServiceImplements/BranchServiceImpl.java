package com.spring.Services.ServiceImplements;

import com.spring.Entity.Branch;
import com.spring.Repository.BranchRepository;
import com.spring.Services.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;

    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Override
    public Branch getBranchById(Long id) {
        Optional<Branch> branch = branchRepository.findById(id);
        if (branch.isPresent()) {
            return branch.get();
        } else {
            throw new RuntimeException("Branch not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public Branch createBranch(Branch branch) {
        // Check if branch name already exists
        if (branchRepository.existsByName(branch.getName())) {
            throw new RuntimeException("Branch with name '" + branch.getName() + "' already exists");
        }

        return branchRepository.save(branch);
    }

    @Override
    @Transactional
    public Branch updateBranch(Long id, Branch branchDetails) {
        Optional<Branch> optionalBranch = branchRepository.findById(id);
        if (optionalBranch.isPresent()) {
            Branch branch = optionalBranch.get();

            // Check if name is being changed and if new name already exists
            if (branchDetails.getName() != null &&
                    !branchDetails.getName().equals(branch.getName()) &&
                    branchRepository.existsByName(branchDetails.getName())) {
                throw new RuntimeException("Branch with name '" + branchDetails.getName() + "' already exists");
            }

            // Update fields if provided
            if (branchDetails.getName() != null) {
                branch.setName(branchDetails.getName());
            }
            if (branchDetails.getAddress() != null) {
                branch.setAddress(branchDetails.getAddress());
            }
            if (branchDetails.getPhoneNumber() != null) {
                branch.setPhoneNumber(branchDetails.getPhoneNumber());
            }

            return branchRepository.save(branch);
        } else {
            throw new RuntimeException("Branch not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        Optional<Branch> branch = branchRepository.findById(id);
        if (branch.isPresent()) {
            branchRepository.deleteById(id);
        } else {
            throw new RuntimeException("Branch not found with id: " + id);
        }
    }

    @Override
    public List<Branch> getUserBranches(UUID userId) {
        // This would typically involve a user-branch relationship table
        // For now, returning all branches as a placeholder
        return branchRepository.findAll();
    }

    @Override
    public List<Branch> searchBranchesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return branchRepository.findAll();
        }
        return branchRepository.findByNameContainingIgnoreCase(name.trim());
    }

    @Override
    public void assignUserToBranch(UUID userId, Long branchId, String role) {
        // Implementation for assigning user to branch
        System.out.println("Assigning user " + userId + " to branch " + branchId + " with role: " + role);
    }
}