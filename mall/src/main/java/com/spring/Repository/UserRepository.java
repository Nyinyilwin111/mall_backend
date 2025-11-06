package com.spring.Repository;

import com.spring.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    // Match the entity field "fullName" (not username)
    User findByFullName(String fullName);

    Optional<User> findByEmail(String email);

    Boolean existsByFullName(String fullName);

    Boolean existsByEmail(String email);

    // Search by fullName or email (correct field names)
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByFullNameOrEmail(@Param("query") String query);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<User> searchByFullName(@Param("fullName") String fullName);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.branches WHERE u.id = :userId")
    Optional<User> findByIdWithBranches(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();

}
