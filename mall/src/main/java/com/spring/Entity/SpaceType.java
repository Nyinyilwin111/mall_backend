package com.spring.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpaceType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "space_type_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "type-name", columnDefinition = "NVARCHAR(100)")
    private String typeName;

    @Column(name = "description", columnDefinition = "NVARCHAR(100)")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
