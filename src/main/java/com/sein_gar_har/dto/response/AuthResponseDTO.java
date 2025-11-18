package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Permission;
import com.sein_gar_har.entity.Role;
import lombok.Data;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class AuthResponseDTO {
    private String token;
    private UUID id;
    private String fullName;
    private String email;
    private Set<RoleDTO> roles;
    private boolean isAuthenticated;

    public AuthResponseDTO(String token, UUID id, String fullName, String email, Set<Role> roles) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles != null ? roles.stream()
                .map(RoleDTO::new)
                .collect(Collectors.toSet()) : Set.of();
        this.isAuthenticated = true;
    }

    @Data
    public static class RoleDTO {
        private Long id;
        private String name;
        private String description;
        private Set<PermissionDTO> permissions;

        public RoleDTO(Role role) {
            this.id = role.getId();
            this.name = role.getName();
            this.description = role.getDescription();
            this.permissions = role.getPermissions() != null ?
                    role.getPermissions().stream()
                            .map(PermissionDTO::new)
                            .collect(Collectors.toSet()) : Set.of();
        }
    }

    @Data
    public static class PermissionDTO {
        private Long id;
        private String name;
        private String description;

        public PermissionDTO(Permission permission) {
            this.id = permission.getId();
            this.name = permission.getName();
            this.description = permission.getDescription();
        }
    }
}