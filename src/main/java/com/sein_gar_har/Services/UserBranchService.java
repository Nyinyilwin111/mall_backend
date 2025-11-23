package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.UserBranchRequestDTO;
import com.sein_gar_har.dto.response.UserResponseDTO;

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