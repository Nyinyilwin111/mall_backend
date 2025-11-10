package com.spring.Services;

import com.spring.DTO.response.BranchResponseDTO;
import com.spring.DTO.request.CreateBranchRequestDTO;
import com.spring.DTO.request.UpdateBranchRequestDTO;

import java.security.Principal;
import java.util.List;

public interface BranchService {
    List<BranchResponseDTO> getAllBranches();
    List<BranchResponseDTO> getBranchesForCurrentUser();
    BranchResponseDTO getBranchById(Long id);
    BranchResponseDTO createBranch(CreateBranchRequestDTO request);
    BranchResponseDTO updateBranch(Long id, UpdateBranchRequestDTO request);
    void deleteBranch(Long id);
    boolean existsByName(String name);

}