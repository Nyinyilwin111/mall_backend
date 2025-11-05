package com.spring.Repository;

import com.spring.Entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    // Search by branch name (case-insensitive)
    List<Branch> findByNameContainingIgnoreCase(String name);

    // Search by address (case-insensitive)
    List<Branch> findByAddressContainingIgnoreCase(String address);

    // Search by phone number
    List<Branch> findByPhoneNumberContaining(String phoneNumber);

    // Combined search by name and address
    @Query("SELECT b FROM Branch b WHERE " +
            "(:name IS NULL OR :name = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:address IS NULL OR :address = '' OR LOWER(b.address) LIKE LOWER(CONCAT('%', :address, '%')))")
    List<Branch> findByNameContainingAndAddressContaining(
            @Param("name") String name,
            @Param("address") String address
    );



    // Check if branch name exists
    boolean existsByName(String name);

    // Check if branch name exists excluding a specific branch (for updates)
    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.name = :name AND b.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    // Find all branches ordered by name
    List<Branch> findAllByOrderByNameAsc();
}