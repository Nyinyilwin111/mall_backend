package com.spring.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")  //exclude password from toString() to avoid leaking sensitive data.
@EqualsAndHashCode(of = "email")  //The same email → same hash → consistent behavior in sets/maps.
@Entity(name = "APP_USER")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(unique = true)
    private String email;
    private String password;
    private String fullName;
}

