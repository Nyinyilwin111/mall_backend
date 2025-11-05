package com.spring.Services.ServiceImplements;

import com.spring.DTO.response.BranchResponseDTO;
import com.spring.DTO.request.CreateBranchRequestDTO;
import com.spring.DTO.request.UpdateBranchRequestDTO;
import com.spring.Entity.Branch;
import com.spring.Repository.BranchRepository;
import com.spring.Services.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public List<BranchResponseDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponseDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return convertToResponse(branch);
    }

    @Override
    public BranchResponseDTO createBranch(CreateBranchRequestDTO request) {
        if (branchRepository.existsByName(request.getName())) {
            throw new RuntimeException("Branch name already exists: " + request.getName());
        }

        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhoneNumber(request.getPhoneNumber());

        Branch savedBranch = branchRepository.save(branch);
        return convertToResponse(savedBranch);
    }

    @Override
    public BranchResponseDTO updateBranch(Long id, UpdateBranchRequestDTO request) {
        Branch existingBranch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

        if (!existingBranch.getName().equals(request.getName()) &&
                branchRepository.existsByName(request.getName())) {
            throw new RuntimeException("Branch name already exists: " + request.getName());
        }

        existingBranch.setName(request.getName());
        existingBranch.setAddress(request.getAddress());
        existingBranch.setPhoneNumber(request.getPhoneNumber());

        Branch updatedBranch = branchRepository.save(existingBranch);
        return convertToResponse(updatedBranch);
    }

    @Override
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new RuntimeException("Branch not found with id: " + id);
        }
        branchRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }

    private BranchResponseDTO convertToResponse(Branch branch) {
        BranchResponseDTO response = new BranchResponseDTO();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());
        response.setPhoneNumber(branch.getPhoneNumber());
        response.setCreatedAt(branch.getCreatedAt());
        response.setUpdatedAt(branch.getUpdatedAt());
        return response;
    }
}