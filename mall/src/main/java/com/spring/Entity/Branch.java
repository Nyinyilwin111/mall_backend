package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@Entity
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // <- make sure strategy is set
    @UuidGenerator  // optional if you want UUID
    private UUID id;

    @Column(nullable = false)
    private String name;
}
