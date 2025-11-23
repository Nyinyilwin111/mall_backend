package com.sein_gar_har.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "space")
public class Space {

    @Id
    @Column(name = "space_id")
    private UUID spaceId;

    @Column(name = "space_code", unique = true, nullable = false, length = 50)
    private String spaceCode; // ADD THIS FIELD - Unique identifier for the space

    @ManyToOne
    @JoinColumn(name = "space_type_id")
    private SpaceType spaceType;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "size_sqft", scale = 2)
    private Double sizeSqft;

    @Column(name = "price", scale = 2) // ADD THIS FIELD - Price of the space
    private Double price;

    @Column(columnDefinition = "JSON")
    private String amenities;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SpaceStatus status;

    @ElementCollection
    @CollectionTable(name = "space_images", joinColumns = @JoinColumn(name = "space_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "Floor_floor_id")
    private Floor floor;

    // Constructors
    public Space() {
        this.spaceId = UUID.randomUUID();
        this.status = SpaceStatus.VACANT;
    }

    public Space(String spaceCode, SpaceType spaceType, String location, Double sizeSqft, Double price, String amenities, Floor floor) {
        this();
        this.spaceCode = spaceCode;
        this.spaceType = spaceType;
        this.location = location;
        this.sizeSqft = sizeSqft;
        this.price = price;
        this.amenities = amenities;
        this.floor = floor;
    }

    // Add SpaceStatus enum
    public enum SpaceStatus {
        VACANT, OCCUPIED, MAINTENANCE, RESERVED
    }
}