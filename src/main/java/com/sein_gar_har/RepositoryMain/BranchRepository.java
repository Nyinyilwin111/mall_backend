package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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


//    for accounting
@Query("SELECT COUNT(s) FROM Space s WHERE s.floor.branchBranchId = :branchId")
Long countTotalSpaces(@Param("branchId") Integer branchId);

    @Query("SELECT COUNT(s) FROM Space s WHERE s.status = 'OCCUPIED' AND s.floor.branchBranchId = :branchId")
    Long countOccupiedSpaces(@Param("branchId") Integer branchId);

    @Query("SELECT COALESCE(AVG(l.rentAmount), 0) FROM Lease l WHERE l.status = 'ACTIVE' AND l.space.floor.branchBranchId = :branchId")
    BigDecimal getAverageRent(@Param("branchId") Integer branchId);

    // Payment statistics
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' " +
            "AND (:branchId IS NULL OR p.lease.space.floor.branchBranchId = :branchId)")
    BigDecimal getTotalRevenue(@Param("branchId") Integer branchId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' " +
            "AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate " +
            "AND (:branchId IS NULL OR p.lease.space.floor.branchBranchId = :branchId)")
    BigDecimal getRevenueInPeriod(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("branchId") Integer branchId);

    // Accounts Receivable calculations
    @Query("SELECT l FROM Lease l WHERE l.status = 'ACTIVE' " +
            "AND (:branchId IS NULL OR l.space.floor.branchBranchId = :branchId)")
    List<Lease> getActiveLeases(@Param("branchId") Integer branchId);

    @Query("SELECT p FROM Payment p WHERE p.lease.leaseId = :leaseId AND p.status = 'PAID'")
    List<Payment> getPaymentsByLeaseId(@Param("leaseId") Long leaseId);

    // Overdue payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'OVERDUE' " +
            "AND (:branchId IS NULL OR p.lease.space.floor.branchBranchId = :branchId)")
    BigDecimal getTotalOverdueAmount(@Param("branchId") Integer branchId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PENDING' " +
            "AND p.paymentDate <= :dueDate " +
            "AND (:branchId IS NULL OR p.lease.space.floor.branchBranchId = :branchId)")
    BigDecimal getPendingAmountByDueDate(@Param("dueDate") LocalDate dueDate,
                                         @Param("branchId") Integer branchId);

    // Monthly payment data for cash flow
    @Query("SELECT MONTH(p.paymentDate) as month, COALESCE(SUM(p.amount), 0) as amount " +
            "FROM Payment p WHERE p.status = 'PAID' AND YEAR(p.paymentDate) = :year " +
            "AND (:branchId IS NULL OR p.lease.space.floor.branchBranchId = :branchId) " +
            "GROUP BY MONTH(p.paymentDate) ORDER BY MONTH(p.paymentDate)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year, @Param("branchId") Integer branchId);

    // Expense calculations
    @Query("SELECT COALESCE(SUM(u.amount), 0) FROM Utility u WHERE u.recordStatus = 'ACTIVE' " +
            "AND u.utilityType = 'MAINTENANCE' " +
            "AND (:branchId IS NULL OR u.space.floor.branchBranchId = :branchId)")
    BigDecimal getMaintenanceCosts(@Param("branchId") Integer branchId);

    @Query("SELECT COALESCE(SUM(u.amount), 0) FROM Utility u WHERE u.recordStatus = 'ACTIVE' " +
            "AND u.utilityType IN ('ELECTRICITY', 'WATER', 'GAS') " +
            "AND (:branchId IS NULL OR u.space.floor.branchBranchId = :branchId)")
    BigDecimal getUtilityCosts(@Param("branchId") Integer branchId);

    // Lease statistics
    @Query("SELECT COUNT(l) FROM Lease l WHERE l.status = 'ACTIVE' " +
            "AND l.startDate >= :startDate " +
            "AND (:branchId IS NULL OR l.space.floor.branchBranchId = :branchId)")
    Long countNewLeases(@Param("startDate") LocalDate startDate, @Param("branchId") Integer branchId);

    @Query("SELECT COUNT(l) FROM Lease l WHERE l.status = 'EXPIRED' " +
            "AND l.endDate >= :startDate " +
            "AND (:branchId IS NULL OR l.space.floor.branchBranchId = :branchId)")
    Long countExpiredLeases(@Param("startDate") LocalDate startDate, @Param("branchId") Integer branchId);

    // Payment aging analysis
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'OVERDUE') " +
            "AND (:branchId IS NULL OR p.lease.space.floor.branchBranchId = :branchId)")
    List<Payment> getPendingAndOverduePayments(@Param("branchId") Integer branchId);
}