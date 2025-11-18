package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Role;
import lombok.Data;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RoleResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Set<PermissionResponseDTO> permissions;

    public RoleResponseDTO(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream()
                .map(PermissionResponseDTO::new)
                .collect(Collectors.toSet());
    }
}