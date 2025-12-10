package com.sein_gar_har.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class UpdateAvatarRequest {
    @NotNull
    private String userId;

    @NotBlank
    private String avatarUrl;

}