
package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "lease")
public class Lease {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lease_id")
    private Long leaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_user_id", nullable = false)
    private User tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "rent_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "deposit_amount", precision = 12, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "contract_doc_url")
    private String contractDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaseStatus status = LeaseStatus.DRAFT;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "tenant_type", length = 50)
    private String tenantType;

    @Column(name = "tenant_trade", length = 100)
    private String tenantTrade;

    @Column(name = "NRC", length = 50)
    private String nrc;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "heir", length = 100)
    private String heir;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum LeaseStatus {
        DRAFT, ACTIVE, EXPIRED, TERMINATED
    }

    // Constructors
    public Lease() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}