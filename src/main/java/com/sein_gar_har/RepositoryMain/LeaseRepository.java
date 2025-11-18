package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {

    @Query("SELECT l FROM Lease l WHERE l.tenant.id = :tenantId")
    List<Lease> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT l FROM Lease l WHERE l.space.spaceId = :spaceId")
    List<Lease> findAllBySpaceId(@Param("spaceId") UUID spaceId);

    @Query("SELECT l FROM Lease l JOIN l.space s JOIN s.floor f WHERE l.tenant.id = :tenantId AND f.branchBranchId = :branchId")
    List<Lease> findAllByTenantIdAndBranchId(@Param("tenantId") UUID tenantId, @Param("branchId") Long branchId);
}
