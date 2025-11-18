package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Permission;
import lombok.Data;

@Data
public class PermissionResponseDTO {
    private Long id;
    private String name;

    public PermissionResponseDTO(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
    }
}