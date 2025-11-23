package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByFullName(String name);

    Boolean existsByFullName(String fullName);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:fullName%")
    List<User> findByName(@Param("fullName") String fullName);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:query% OR u.email LIKE %:query%")
    List<User> findByFullNameOrEmail(@Param("query") String query);

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

    // Find all users by branch
    @Query("SELECT u FROM User u JOIN u.branches b WHERE b = :branch")
    List<User> findUsersByBranch(@Param("branch") Branch branch);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.branches WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndBranches(@Param("email") String email);

    // Correct query to find users by role name through the roles relationship
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRole(@Param("roleName") String roleName);


}
