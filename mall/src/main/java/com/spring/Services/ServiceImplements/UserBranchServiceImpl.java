package com.spring.Services.ServiceImplements;


import com.spring.DTO.response.BranchResponseDTO;
import com.spring.DTO.request.UserBranchRequestDTO;
import com.spring.DTO.response.UserResponseDTO;
import com.spring.Entity.Branch;
import com.spring.Entity.User;
import com.spring.Repository.BranchRepository;
import com.spring.Repository.UserRepository;
import com.spring.Services.UserBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBranchServiceImpl implements UserBranchService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public UserResponseDTO assignBranchesToUser(UserBranchRequestDTO userBranchRequestDTO) {
        UUID userId = userBranchRequestDTO.getUserId();
        Set<Long> branchIds = userBranchRequestDTO.getBranchIds();

        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Find all branches by their IDs
        Set<Branch> branchesToAdd = new HashSet<>(branchRepository.findByIds(branchIds));

        if (branchesToAdd.size() != branchIds.size()) {
            throw new RuntimeException("One or more branches not found");
        }

        // Add branches to user
        for (Branch branch : branchesToAdd) {
            user.addBranch(branch);
        }

        User savedUser = userRepository.save(user);
        log.info("Assigned {} branches to user: {}", branchesToAdd.size(), user.getEmail());

        return convertToUserResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO removeBranchesFromUser(UserBranchRequestDTO userBranchRequestDTO) {
        UUID userId = userBranchRequestDTO.getUserId();
        Set<Long> branchIds = userBranchRequestDTO.getBranchIds();

        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Find all branches by their IDs
        Set<Branch> branchesToRemove = new HashSet<>(branchRepository.findByIds(branchIds));

        // Remove branches from user
        for (Branch branch : branchesToRemove) {
            user.removeBranch(branch);
        }

        User savedUser = userRepository.save(user);
        log.info("Removed {} branches from user: {}", branchesToRemove.size(), user.getEmail());

        return convertToUserResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserWithBranches(UUID userId) {
        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToUserResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserBranches(UserBranchRequestDTO userBranchRequestDTO) {
        UUID userId = userBranchRequestDTO.getUserId();
        Set<Long> branchIds = userBranchRequestDTO.getBranchIds();

        User user = userRepository.findByIdWithBranches(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Clear existing branches
        user.getBranches().clear();

        // Find and add new branches
        Set<Branch> newBranches = new HashSet<>(branchRepository.findByIds(branchIds));

        if (newBranches.size() != branchIds.size()) {
            throw new RuntimeException("One or more branches not found");
        }

        for (Branch branch : newBranches) {
            user.addBranch(branch);
        }

        User savedUser = userRepository.save(user);
        log.info("Updated branches for user: {}, now has {} branches", user.getEmail(), newBranches.size());

        return convertToUserResponseDTO(savedUser);
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFullName(user.getFullName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setEnabled(user.isEnabled());

        // Convert roles
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName()) // Assuming Role entity has getName() method
                .collect(Collectors.toSet());
        userResponseDTO.setRoles(roleNames);

        // Convert branches
        Set<BranchResponseDTO> branchDTOs = user.getBranches().stream()
                .map(this::convertToBranchResponseDTO)
                .collect(Collectors.toSet());
        userResponseDTO.setBranches(branchDTOs);

        return userResponseDTO;
    }

    private BranchResponseDTO convertToBranchResponseDTO(Branch branch) {
        BranchResponseDTO branchResponseDTO = new BranchResponseDTO();
        branchResponseDTO.setId(branch.getId());
        branchResponseDTO.setName(branch.getName());
        branchResponseDTO.setAddress(branch.getAddress());
        branchResponseDTO.setPhoneNumber(branch.getPhoneNumber());
        branchResponseDTO.setCreatedAt(branch.getCreatedAt());
        branchResponseDTO.setUpdatedAt(branch.getUpdatedAt());
        return branchResponseDTO;
    }
}