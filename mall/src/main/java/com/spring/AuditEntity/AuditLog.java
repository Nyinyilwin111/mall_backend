package com.spring.AuditEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "log_id", columnDefinition = "BINARY(16)")
    private UUID logId;

    @Column(name = "action", columnDefinition = "NVARCHAR(100)")
    private String action;

    // this column and
    @Column(name = "table_affected", columnDefinition = "NVARCHAR(100)")
    private String tableAffected;

    // this column
    @Column(name = "record_id", columnDefinition = "NVARCHAR(100)")
    private String recordId;

    @Column(name = "old_values", columnDefinition = "NVARCHAR(100)")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "NVARCHAR(100)")
    private String newValues;

    @Column(name = "ip_address", columnDefinition = "NVARCHAR(100)")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "NVARCHAR(100)")
    private String userAgent;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
