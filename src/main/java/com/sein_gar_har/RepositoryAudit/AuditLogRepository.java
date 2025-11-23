// AuditLogRepository.java
package com.sein_gar_har.RepositoryAudit;

import com.sein_gar_har.auditEntity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // Make sure this method exists and works correctly
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    List<AuditLog> findAllByOrderByTimestampDesc();
}