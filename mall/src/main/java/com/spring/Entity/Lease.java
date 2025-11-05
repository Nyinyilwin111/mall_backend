package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Data
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "lease_id")
    private UUID id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "rent_amount", nullable = false)
    private Double rentAmount;

    @Column(name = "rent_amount", nullable = false)
    private Double depositAmount;

    @Column(name = "contract_doc_url")
    private String contractDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private leaseStatus status = leaseStatus.active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private enum leaseStatus{
        active,done;
    }


}
