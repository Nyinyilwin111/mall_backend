package com.sein_gar_har.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
@Table(name = "spacetype")
public class SpaceType {

    @Id
    @Column(name = "space_type_id")
    private UUID spaceTypeId;

    @Column(name = "type_name", length = 100)
    private String typeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public SpaceType() {
        this.spaceTypeId = UUID.randomUUID();
    }

    public SpaceType(String typeName, String description) {
        this();
        this.typeName = typeName;
        this.description = description;
    }

}