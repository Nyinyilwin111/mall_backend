// AuditLog.java
package com.sein_gar_har.auditEntity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class AuditLog {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID logId;

    private String action;
    private String tableAffected;
    private String recordId;

    @Column(columnDefinition = "TEXT")
    private String oldValues;

    @Column(columnDefinition = "TEXT")
    private String newValues;

    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;

    // Add these new fields for user-friendly messages
    private String performedBy; // Who performed the action (username/email)

    @Column(columnDefinition = "TEXT")
    private String userFriendlyMessage; // Formatted message for frontend
}