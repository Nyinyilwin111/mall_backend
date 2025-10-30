package com.spring.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "floor")
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "floor_id")
    private Integer floorId;

    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "branch_branch_id")
    private Integer branchBranchId;

    // Constructors
    public Floor() {}

    public Floor(String level, Integer branchBranchId) {
        this.level = level;
        this.branchBranchId = branchBranchId;
    }

    }