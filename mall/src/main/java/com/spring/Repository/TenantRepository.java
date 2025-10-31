package com.spring.Repository;
import com.spring.Entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByBranchId(Long branchId);

    Optional<Tenant> findByTenantCode(String tenantCode);

    @Query("SELECT t FROM Tenant t WHERE t.tenantCode = :tenantCode AND t.branch.id = :branchId")
    Optional<Tenant> findByTenantCodeAndBranchId(@Param("tenantCode") String tenantCode, @Param("branchId") Long branchId);

    boolean existsByTenantCodeAndBranchId(String tenantCode, Long branchId);
}