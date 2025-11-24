package com.sein_gar_har.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MaintenanceRequestDTO {

    private UUID tenantId;
    private String title;
    private String description;
    private String priority;
    private String assignedTo;
    private String status;
    private String spaceCode;
}
