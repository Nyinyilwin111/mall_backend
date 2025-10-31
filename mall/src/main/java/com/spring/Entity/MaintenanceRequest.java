package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Data
@Table(name = "maintenance_requests")
public class MaintenanceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private User tenant;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Constructors
    public MaintenanceRequest() {
        this.trackingNumber = generateTrackingNumber();
        this.status = RequestStatus.PENDING;
    }

    public MaintenanceRequest(String title, String description, User tenant) {
        this();
        this.title = title;
        this.description = description;
        this.tenant = tenant;
    }

    private String generateTrackingNumber() {
        return "MR-" + System.currentTimeMillis() + "-" +
                ThreadLocalRandom.current().nextInt(1000, 9999);
    }



    // RequestStatus Enum defined inside the same class
    public enum RequestStatus {
        PENDING,
        IN_PROGRESS,
        RESOLVED
    }
}