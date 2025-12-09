package com.sein_gar_har.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@ToString
@Table(name="notification")
public class PushMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String message;

    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "created_user_id")
    @JsonIgnore
    private User createdUserId;

    @ManyToOne
    @JoinColumn(name = "recipient_user_id")
    @JsonIgnore
    private User recipientUser;

    private boolean readby = false;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch;

    @Column(nullable = false)
    private boolean sentToAll = false;

    // Lease notification fields
    @Column(name = "notification_type")
    private String type;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "lease_id")
    private String leaseId;

    @Column(name = "space_id")
    private String spaceId;

    @Column(name = "space_code")
    private String spaceCode;

    @Column(name = "tenant_name")
    private String tenantName;

    @Column(name = "rent_amount")
    private String rentAmount;

    @Column(name = "created_user_name")
    private String createdUserName;

}