package com.spring.Repository;

import com.spring.Entity.UserBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserBranchRepository extends JpaRepository<UserBranch, UUID> {

//    boolean existsByUserIdAndBranchId(UUID userId, UUID branchId);

    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN TRUE ELSE FALSE END " +
            "FROM UserBranch ub WHERE ub.user.id = :userId AND ub.branch.id = :branchId")
    boolean existsByUserIdAndBranchId(@Param("userId") UUID userId, @Param("branchId") UUID branchId);
}