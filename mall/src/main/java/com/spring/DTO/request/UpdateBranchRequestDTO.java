package com.spring.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBranchRequestDTO {
    @NotBlank(message = "Branch name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String phoneNumber;
}