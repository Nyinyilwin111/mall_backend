package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.CreateBranchRequestDTO;
import com.sein_gar_har.dto.request.UpdateBranchRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;

import java.util.List;

public interface BranchService {
    List<BranchResponseDTO> getAllBranches();
    BranchResponseDTO getBranchById(Long id);
    BranchResponseDTO createBranch(CreateBranchRequestDTO request);
    BranchResponseDTO updateBranch(Long id, UpdateBranchRequestDTO request);
    void deleteBranch(Long id);
    boolean existsByName(String name);
    List<BranchResponseDTO> getBranchesForCurrentUser();

}