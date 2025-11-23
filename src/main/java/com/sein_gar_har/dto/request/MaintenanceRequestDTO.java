package com.sein_gar_har.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MaintenanceRequestDTO {

    private UUID tenantId;
    private String title;
    private String description;
    private String priority;   // e.g. "Low", "Medium", "High"
    private String assignedTo; // optional, for managers to assign
    private String status;     // optional for update, ignored on create
}
