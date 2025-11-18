package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.lease.leaseId = :leaseId")
    List<Payment> findAllByLeaseId(@Param("leaseId") Long leaseId);

    @Query("SELECT p FROM Payment p WHERE p.lease.tenant.id = :tenantId")
    List<Payment> findAllByTenantId(@Param("tenantId") java.util.UUID tenantId);
}