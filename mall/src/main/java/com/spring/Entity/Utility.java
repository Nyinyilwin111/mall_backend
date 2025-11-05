package com.spring.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.beans.factory.aot.AotServices;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Utility {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "reading_id")
    private UUID readingId;

    @Column(name = "utility_type", columnDefinition = "NVARCHAR(20)", nullable = false)
    private String utilityType;

    @Column(name = "period", nullable = false)
    private LocalDateTime period;

    @Column(name = "reading_date")
    private LocalDateTime readingDate;

    @Column(name = "current_reading", nullable = false)
    private Double currentReading;

    @Column(name = "previous_reading", nullable = false)
    private Double previousReading;

    @Column(name = "consumption", nullable = false)
    private Double consumption;

    @Column(name = "allocated_consumption", nullable = false)
    private Double allocatedConsumption;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
