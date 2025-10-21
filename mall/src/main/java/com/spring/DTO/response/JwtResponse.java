package com.spring.DTO.response;

import com.spring.Entity.Role;
import lombok.Data;

import java.util.Set;
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Set<Role> roles;

    public JwtResponse(String token, Long id, String username, String email, Set<Role> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }


}