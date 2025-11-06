package com.spring.DTO.request;

import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class UserBranchRequestDTO {
    private UUID userId;
    private Set<Long> branchIds;
}