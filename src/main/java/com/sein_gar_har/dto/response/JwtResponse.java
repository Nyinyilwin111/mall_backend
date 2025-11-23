package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Role;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private UUID id;
    private String fullName;
    private String email;
    private Set<Role> roles;

    public JwtResponse(String token, UUID id, String fullName, String email, Set<Role> roles) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }


}