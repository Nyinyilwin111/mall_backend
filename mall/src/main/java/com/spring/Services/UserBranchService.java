package com.spring.Services;

import java.util.UUID;

public interface UserBranchService {

    void assignUserToBranch(UUID userId, UUID branchId, String role);
}
