package com.sein_gar_har.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
@Data
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "request_id", columnDefinition = "BINARY(16)")
    private UUID requestId;

    // Who submitted the request (tenant)
    @Column(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID tenantId;

    //    @Column(name = "title", columnDefinition = "NVARCHAR(100)", nullable = false)
    private String title;

    //    @Column(name = "description", columnDefinition = "NVARCHAR(500)", nullable = false)
    private String description;

    // Optional: Low / Medium / High
    @Column(name = "priority", columnDefinition = "NVARCHAR(20)")
    private String priority;

    // Optional: who it is assigned to (staff user-id or name)
    @Column(name = "assigned_to", columnDefinition = "NVARCHAR(100)")
    private String assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private MaintenanceStatus status = MaintenanceStatus.Pending;

    public enum MaintenanceStatus {
        Pending,
        In_Progress,
        Resolved,
        Cancelled
    }
}
