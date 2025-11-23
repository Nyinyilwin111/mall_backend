package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.MaintenanceRequestRepository;
import com.sein_gar_har.Services.MaintenanceRequestService;
import com.sein_gar_har.Services.AuditLogService; // ✅ ADD
import com.sein_gar_har.dto.request.MaintenanceRequestDTO;
import com.sein_gar_har.dto.response.MaintenanceRequestResponse;
import com.sein_gar_har.entity.MaintenanceRequest;
import lombok.RequiredArgsConstructor; // ✅ ADD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap; // ✅ ADD
import java.util.Map; // ✅ ADD
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor // ✅ ADD
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    private final AuditLogService auditLogService; // ✅ ADD

    private MaintenanceRequest.MaintenanceStatus parseStatus(String status) {
        if (status == null) {
            return MaintenanceRequest.MaintenanceStatus.Pending;
        }
        return MaintenanceRequest.MaintenanceStatus.valueOf(status);
    }

    @Override
    public MaintenanceRequestResponse createRequest(MaintenanceRequestDTO dto) {
        MaintenanceRequest entity = new MaintenanceRequest();
        entity.setTenantId(dto.getTenantId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPriority(dto.getPriority());
        entity.setAssignedTo(dto.getAssignedTo());
        entity.setStatus(MaintenanceRequest.MaintenanceStatus.Pending);

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);

        // ✅ AUDIT LOG: Maintenance Request Created
        Map<String, Object> newData = new HashMap<>();
        newData.put("requestId", saved.getRequestId().toString());
        newData.put("tenantId", saved.getTenantId().toString());
        newData.put("title", saved.getTitle());
        newData.put("priority", saved.getPriority());
        newData.put("status", saved.getStatus().toString());
        newData.put("description", saved.getDescription());

        auditLogService.logCreate("MaintenanceRequest", saved.getRequestId().toString(), newData);

        return new MaintenanceRequestResponse(saved);
    }

    @Override
    public List<MaintenanceRequestResponse> getAllRequests() {
        return maintenanceRequestRepository.findAll()
                .stream()
                .map(MaintenanceRequestResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceRequestResponse getRequestById(UUID requestId) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Maintenance request not found"));
        return new MaintenanceRequestResponse(entity);
    }

    @Override
    public List<MaintenanceRequestResponse> getRequestsByTenant(UUID tenantId) {
        return maintenanceRequestRepository.findByTenantId(tenantId)
                .stream()
                .map(MaintenanceRequestResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceRequestResponse updateRequest(UUID requestId, MaintenanceRequestDTO dto) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Maintenance request not found"));

        // ✅ Store old data for audit log
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("requestId", entity.getRequestId().toString());
        oldData.put("title", entity.getTitle());
        oldData.put("description", entity.getDescription());
        oldData.put("priority", entity.getPriority());
        oldData.put("assignedTo", entity.getAssignedTo());
        oldData.put("status", entity.getStatus().toString());

        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getPriority() != null) entity.setPriority(dto.getPriority());
        if (dto.getAssignedTo() != null) entity.setAssignedTo(dto.getAssignedTo());
        if (dto.getStatus() != null) entity.setStatus(parseStatus(dto.getStatus()));

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);

        // ✅ AUDIT LOG: Maintenance Request Updated
        Map<String, Object> newData = new HashMap<>();
        newData.put("requestId", saved.getRequestId().toString());
        newData.put("title", saved.getTitle());
        newData.put("description", saved.getDescription());
        newData.put("priority", saved.getPriority());
        newData.put("assignedTo", saved.getAssignedTo());
        newData.put("status", saved.getStatus().toString());

        auditLogService.logUpdate("MaintenanceRequest", requestId.toString(), oldData, newData);

        return new MaintenanceRequestResponse(saved);
    }

    @Override
    public MaintenanceRequestResponse updateStatus(UUID requestId, String status) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Maintenance request not found"));

        // ✅ Store old status for audit log
        String oldStatus = entity.getStatus().toString();

        entity.setStatus(parseStatus(status));
        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);

        // ✅ AUDIT LOG: Maintenance Request Status Updated
        Map<String, Object> details = new HashMap<>();
        details.put("requestId", requestId.toString());
        details.put("oldStatus", oldStatus);
        details.put("newStatus", saved.getStatus().toString());
        details.put("title", saved.getTitle());

        auditLogService.logAction(
                "MAINTENANCE_STATUS_UPDATE",
                "MaintenanceRequest",
                requestId.toString(),
                null,
                details
        );

        return new MaintenanceRequestResponse(saved);
    }

    @Override
    public void deleteRequest(UUID requestId) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Maintenance request not found with ID: " + requestId));

        // ✅ Store data for audit log before deletion
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("requestId", entity.getRequestId().toString());
        oldData.put("tenantId", entity.getTenantId().toString());
        oldData.put("title", entity.getTitle());
        oldData.put("priority", entity.getPriority());
        oldData.put("status", entity.getStatus().toString());
        oldData.put("description", entity.getDescription());

        maintenanceRequestRepository.deleteById(requestId);

        // ✅ AUDIT LOG: Maintenance Request Deleted
        auditLogService.logDelete("MaintenanceRequest", requestId.toString(), oldData);
    }
}