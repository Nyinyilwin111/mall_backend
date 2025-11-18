package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.dto.request.CreateBranchRequestDTO;
import com.sein_gar_har.dto.request.UpdateBranchRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;

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

    @Override
    public List<BranchResponseDTO> getBranchesForCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            System.out.println("=== DEBUG: getBranchesForCurrentUser ===");
            System.out.println("Authentication: " + authentication);
            System.out.println("Username from token: " + username);
            System.out.println("Principal: " + authentication.getPrincipal());
            System.out.println("Authorities: " + authentication.getAuthorities());

            // Try to find user by username (which might be the email)
            Optional<User> userOptional = userRepository.findByEmail(username);

            if (userOptional.isEmpty()) {
                System.out.println("User not found by email: " + username);

                // Try to find by fullName as fallback
                Optional<User> userByFullName = userRepository.findByFullName(username);
                if (userByFullName.isEmpty()) {
                    System.out.println("User found by fullName: " + username);
                    User user = userByFullName.get();
                    return getUserBranches(user);
                } else {
                    System.out.println("User not found by fullName either: " + username);
                    throw new RuntimeException("User not found: " + username);
                }
            }

            User user = userOptional.get();
            System.out.println("User found: " + user.getEmail() + ", fullName: " + user.getFullName());

            return getUserBranches(user);

        } catch (Exception e) {
            System.err.println("Error in getBranchesForCurrentUser: " + e.getMessage());
            e.printStackTrace();
            // Fallback to empty list instead of throwing exception
            return List.of();
        }
    }

    private List<BranchResponseDTO> getUserBranches(User user) {
        // Check if user has tenant role
        boolean isTenant = user.getRoles().stream()
                .anyMatch(role -> "TENANT".equalsIgnoreCase(role.getName()));

        System.out.println("User " + user.getEmail() + " is tenant: " + isTenant);
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