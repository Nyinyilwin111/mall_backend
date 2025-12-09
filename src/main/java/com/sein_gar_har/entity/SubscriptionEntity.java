package com.sein_gar_har.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@Entity
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 65535)
    private String endpoint;

    @Column(nullable = false, length = 65535)
    private String p256dh;

    @Column(nullable = false, length = 65535)
    private String auth;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
