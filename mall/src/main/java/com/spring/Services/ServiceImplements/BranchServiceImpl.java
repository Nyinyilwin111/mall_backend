package com.spring.Services.ServiceImplements;

import com.spring.DTO.response.BranchResponseDTO;
import com.spring.DTO.request.CreateBranchRequestDTO;
import com.spring.DTO.request.UpdateBranchRequestDTO;
import com.spring.Entity.Branch;
import com.spring.Entity.User;
import com.spring.Repository.BranchRepository;
import com.spring.Repository.UserRepository;
import com.spring.Services.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository; // Add this

    @Override
    public List<BranchResponseDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BranchResponseDTO> getBranchesForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Use the new repository method to fetch user with roles and branches
        User user = userRepository.findByEmailWithRolesAndBranches(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user has tenant role
        boolean isTenant = user.getRoles().stream()
                .anyMatch(role -> "TENANT".equalsIgnoreCase(role.getName()));

        System.out.println("User " + username + " is tenant: " + isTenant);
        System.out.println("User has " + user.getBranches().size() + " assigned branches");

        if (isTenant) {
            // Return only branches assigned to this tenant
            List<BranchResponseDTO> tenantBranches = user.getBranches().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            System.out.println("Returning " + tenantBranches.size() + " branches for tenant");
            return tenantBranches;
        } else {
            // Return all branches for admin/manager users
            List<BranchResponseDTO> allBranches = branchRepository.findAll().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            System.out.println("Returning " + allBranches.size() + " branches for admin/manager");
            return allBranches;
        }
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