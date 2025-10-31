package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@Entity
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Ensure generation strategy
    @UuidGenerator
    private UUID id;  // <- Mandatory @Id

    @Column(nullable = false, length = 65535) // optional, aligns with TEXT
    private String endpoint;

    @Column(nullable = false, length = 65535)
    private String p256dh;

    @Column(nullable = false, length = 65535)
    private String auth;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // optional back-reference
}
