package com.sein_gar_har.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "utility")
public class Utility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utility_id")
    private Long utilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    // ✅ ADDED: Tenant ID field
    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "utility_type", nullable = false, length = 50)
    private String utilityType;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "billing_period")
    private String billingPeriod;

    @Column(name = "usage_unit")
    private String usageUnit;

    @Column(name = "previous_reading")
    private Double previousReading;

    @Column(name = "current_reading")
    private Double currentReading;

    @Column(name = "usage_amount")
    private Double usageAmount;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "record_status", length = 20)
    private String recordStatus = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Utility() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateUsageAmount() {
        if (this.previousReading != null && this.currentReading != null) {
            this.usageAmount = this.currentReading - this.previousReading;
        }
    }
}