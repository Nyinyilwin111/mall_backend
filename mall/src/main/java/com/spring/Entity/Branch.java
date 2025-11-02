package com.spring.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // <- make sure strategy is set
    @UuidGenerator  // optional if you want UUID
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phoneNumber;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserBranch> userBranches = new ArrayList<>();
}
