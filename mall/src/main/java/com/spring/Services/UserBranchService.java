package com.spring.Services;

import com.spring.DTO.request.UserBranchRequestDTO;
import com.spring.DTO.response.UserResponseDTO;

import java.util.UUID;

public interface UserBranchService {

    // Assign branches to a user
    UserResponseDTO assignBranchesToUser(UserBranchRequestDTO userBranchRequestDTO);

    // Remove branches from a user
    UserResponseDTO removeBranchesFromUser(UserBranchRequestDTO userBranchRequestDTO);

    // Get user with branches
    UserResponseDTO getUserWithBranches(UUID userId);

    // Update user branches (replace existing branches)
    UserResponseDTO updateUserBranches(UserBranchRequestDTO userBranchRequestDTO);
}