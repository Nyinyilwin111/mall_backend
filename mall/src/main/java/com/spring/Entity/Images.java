package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;

import java.util.UUID;
@Entity
@Data
public class Images {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "image_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "image_url", columnDefinition = "NVARCHAR(255)")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

}
