package com.spring.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // <- make sure strategy is set
    @UuidGenerator  // optional if you want UUID
    private UUID spaceId;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false, length = 100)
    private Double sizeSqft;

    @Column(name = "amenities", columnDefinition = "JSON")
    private String amenities;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at", insertable = false)
    private LocalDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Images> image = new ArrayList<>();

}
