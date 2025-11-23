package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    List<MaintenanceRequest> findByTenantId(UUID tenantId);

    List<MaintenanceRequest> findByStatus(MaintenanceRequest.MaintenanceStatus status);
}
