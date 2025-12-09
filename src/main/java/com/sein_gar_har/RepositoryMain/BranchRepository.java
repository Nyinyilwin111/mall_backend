package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
//
//    // Search by branch name (case-insensitive)
//    List<Branch> findByNameContainingIgnoreCase(String name);
//
//    // Search by address (case-insensitive)
//    List<Branch> findByAddressContainingIgnoreCase(String address);
//
//    // Search by phone number
//    List<Branch> findByPhoneNumberContaining(String phoneNumber);

    // Combined search by name and address
    @Query("SELECT b FROM Branch b WHERE " +
            "(:name IS NULL OR :name = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:address IS NULL OR :address = '' OR LOWER(b.address) LIKE LOWER(CONCAT('%', :address, '%')))")
    List<Branch> findByNameContainingAndAddressContaining(
            @Param("name") String name,
            @Param("address") String address
    );


    // Check if branch name exists excluding a specific branch (for updates)
    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.name = :name AND b.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    // Find all branches ordered by name
    List<Branch> findAllByOrderByNameAsc();

    Optional<Branch> findByName(String name);
    Boolean existsByName(String name);

    @Query("SELECT b FROM Branch b WHERE b.id IN :branchIds")
    List<Branch> findByIds(@Param("branchIds") Set<Long> branchIds);

    @Query("SELECT COUNT(f) FROM Floor f WHERE f.branchBranchId = :branchId")
    long countFloorsByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(s) FROM Space s " +
            "JOIN Floor f ON s.floor.floorId = f.floorId " +
            "WHERE f.branchBranchId = :branchId")
    long countSpacesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.users")
    List<Branch> findAllWithUsers();

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.users WHERE b.id = :id")
    Optional<Branch> findByIdWithUsers(Long id);
}