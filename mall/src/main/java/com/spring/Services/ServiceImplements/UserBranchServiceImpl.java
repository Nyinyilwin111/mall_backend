package com.spring.Services.ServiceImplements;

import com.spring.Entity.Branch;
import com.spring.Entity.User;
import com.spring.Entity.UserBranch;
import com.spring.Exceptions.UserException;
import com.spring.Repository.UserBranchRepository;
import com.spring.Services.BranchService;
import com.spring.Services.UserBranchService;
import com.spring.Services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserBranchServiceImpl implements UserBranchService {

    @Autowired
    private UserService userService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private UserBranchRepository userBranchRepository;

    @Override
    @Transactional
    public void assignUserToBranch(UUID userId, UUID branchId, String role) {
        // Fetch the User
        User user;
        try {
            user = userService.findUserById(userId);
        } catch (UserException e) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        // Fetch the Branch
        Optional<Branch> branch= branchService.getBranchById(branchId);
        if(branch.isEmpty()){
            throw new EntityNotFoundException("Branch not found with id: " + branchId);
        }

        // Check if already assigned
        boolean alreadyAssigned = userBranchRepository.existsByUserIdAndBranchId(userId, branchId);
        if (alreadyAssigned) {
            throw new IllegalStateException("User is already assigned to this branch");
        }

        // Create the association
        UserBranch userBranch = new UserBranch();
        userBranch.setUser(user);
        userBranch.setBranch(branch.get());
        userBranch.setRole(role);

        // Save the association
        userBranchRepository.save(userBranch);
    }
}
