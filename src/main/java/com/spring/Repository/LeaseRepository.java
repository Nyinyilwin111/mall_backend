package com.spring.Repository;

import com.spring.Entity.Lease;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

	@Query("SELECT l FROM Lease l WHERE l.tenant.id = :tenantId")
    List<Lease> findAllByTenantId(@Param("tenantId") UUID tenantId);
}
