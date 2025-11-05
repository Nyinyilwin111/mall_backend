package com.spring.Services.ServiceImplements;

import com.spring.Entity.Branch;
import com.spring.RepositoryMain.BranchRepository;
import com.spring.Services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    BranchRepository branchRepository;


    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Override
    @Transactional
    public Optional<Branch> getBranchById(UUID id) {
        Optional<Branch> branch = branchRepository.findById(id);
        if (branch.isPresent()) {
            return Optional.of(branch.get());
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
    public Branch updateBranch(UUID id, Branch branchDetails) {
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
    public void deleteBranch(UUID id) {
        Optional<Branch> branch = branchRepository.findById(id);
        if (branch.isPresent()) {
            branchRepository.deleteById(id);
        } else {
            throw new RuntimeException("Branch not found with id: " + id);
        }
    }

    @Override
    public List<Branch> getUserBranches(UUID userId) {
        return branchRepository.findByUserId(userId);
    }

    @Override
    public List<Branch> searchBranchesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return branchRepository.findAll();
        }
        return branchRepository.findByNameContainingIgnoreCase(name.trim());
    }
}

