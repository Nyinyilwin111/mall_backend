package com.sein_gar_har.dto.response;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

//@Builder
//public record LoginResponseDTO(String token, boolean isAuthenticated) {
//
//}

@Builder
public record LoginResponseDTO(
        UUID id,
        String token,
        boolean isAuthenticated,
        String fullName,
        String email,
        boolean enabled,
        Set<RoleResponseDTO> roles,
        Set<BranchResponseDTO> branches

        ) {

}
