package com.spring.Entity;

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
public class PushMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // ensure generation strategy is set
    @UuidGenerator
    private UUID id;  // <- This is mandatory

    @Column(nullable = false)
    private String message;

    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "created_user_id")
    @JsonIgnore
    private User createdUserId; // the sender

    @ManyToOne
    @JoinColumn(name = "recipient_user_id")
    @JsonIgnore
    private User recipientUser;

    private boolean readby= false;


    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch; // optional for branch-specific messages

    @Column(nullable = false)
    private boolean sentToAll = false; // true if sent to all users
}
