package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.MaintenanceRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaintenanceRequestResponse {

    private UUID requestId;
    private UUID tenantId;
    private String title;
    private String description;
    private String priority;
    private String assignedTo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String spaceCode;

    public MaintenanceRequestResponse(MaintenanceRequest entity) {
        this.requestId = entity.getRequestId();
        this.tenantId = entity.getTenantId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.priority = entity.getPriority();
        this.assignedTo = entity.getAssignedTo();
        this.status = entity.getStatus().name();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.spaceCode = entity.getSpaceCode();
    }
}
