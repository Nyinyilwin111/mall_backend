package com.spring.DTO.response;

import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String fullName;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private Set<BranchResponseDTO> branches;
}