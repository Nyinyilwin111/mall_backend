package com.spring.DTO.response;

import com.spring.Entity.User;
import lombok.Builder;

import java.util.*;
import java.util.stream.Collectors;

@Builder
public record UserDTO(
        UUID id,
        String email,
        String fullName,
        boolean enabled,
        Set<String> roles,
        Set<BranchResponseDTO> branches
) {

    public static UserDTO fromUser(User user) {
        if (Objects.isNull(user)) return null;

        // Convert roles to role names
        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            roleNames = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
        }

        // Convert branches to BranchResponseDTO
        Set<BranchResponseDTO> branchDTOs = new HashSet<>();
        if (user.getBranches() != null) {
            branchDTOs = user.getBranches().stream()
                    .map(branch -> {
                        BranchResponseDTO branchDTO = new BranchResponseDTO();
                        branchDTO.setId(branch.getId());
                        branchDTO.setName(branch.getName());
                        branchDTO.setAddress(branch.getAddress());
                        branchDTO.setPhoneNumber(branch.getPhoneNumber());
                        branchDTO.setCreatedAt(branch.getCreatedAt());
                        branchDTO.setUpdatedAt(branch.getUpdatedAt());
                        return branchDTO;
                    })
                    .collect(Collectors.toSet());
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.isEnabled())
                .roles(roleNames)
                .branches(branchDTOs)
                .build();
    }

    public static Set<UserDTO> fromUsers(Collection<User> users) {
        if (Objects.isNull(users)) return Set.of();
        return users.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toSet());
    }

    public static List<UserDTO> fromUsersAsList(Collection<User> users) {
        if (Objects.isNull(users)) return List.of();
        return users.stream()
                .map(UserDTO::fromUser)
                .toList();
    }
}