package com.sein_gar_har.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String password;

    private boolean enabled = true;

    @Column(name = "avatar_url")
    private String avatarUrl;

    // this is subscriptions entity mapping
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Set<SubscriptionEntity> subscriptions = new HashSet<>();

    // this is role entity mapping
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private Images profileImage;

    // this is branch entity mapping
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "user_branches",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "branch_id")
    )
    private Set<Branch> branches = new HashSet<>();

    public User(String fullName, String password, String email) {
        this.fullName = fullName;
        this.password = password;
        this.email = email;
        this.enabled = true;
    }

    public String getAvatarUrl() {
        // Return profileImage URL if exists, otherwise return direct avatarUrl
        if (profileImage != null && profileImage.getImageUrl() != null) {
            return profileImage.getImageUrl();
        }
        return avatarUrl;
    }

    // Utility methods
    public void addBranch(Branch branch) {
        branches.add(branch);
        branch.getUsers().add(this);
    }

    public void removeBranch(Branch branch) {
        branches.remove(branch);
        branch.getUsers().remove(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return Objects.equals(email, other.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }

}
