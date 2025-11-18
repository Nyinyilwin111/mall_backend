package com.sein_gar_har.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    // Constructors
    public Permission() {
    }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }

}