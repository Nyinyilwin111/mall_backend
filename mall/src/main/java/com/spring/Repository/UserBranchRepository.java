package com.spring.Repository;

import com.spring.Entity.UserBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBranchRepository extends JpaRepository<UserBranch, Long> {
    boolean existsByUserIdAndBranchId(UUID userId, Long branchId);
}