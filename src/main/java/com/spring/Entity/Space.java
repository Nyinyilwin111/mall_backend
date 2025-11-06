// Space.java
package com.spring.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "space")
public class Space {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "space_name")
    private String spaceName;

    @Column(name = "location")
    private String location;

    @Column(name = "area_sqft")
    private Double areaSqft;

    @Column(name = "monthly_rent")
    private Double monthlyRent;

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}