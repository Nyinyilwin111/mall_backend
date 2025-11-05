package com.spring.Entity;

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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "payment_id")
    private UUID paymentId;

    private Double amount;

    private LocalDateTime paymentDate;

    @Column(name="proof_image_url",nullable = false)
    private String proofImageUrl;

    @Column(name="staus")
    private paymentStatus status = paymentStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private enum paymentStatus{
        PENDING,COMPLETED,REFUNDED,REMAIN;
    }

}
