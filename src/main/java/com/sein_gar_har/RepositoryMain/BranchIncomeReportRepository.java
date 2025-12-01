//package com.sein_gar_har.RepositoryMain;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public interface BranchIncomeReportRepository extends JpaRepository<Object, Long> {
//
//    @Query(value = """
//        SELECT
//            b.id as branchId,
//            b.name as branchName,
//            p.payment_id as paymentId,
//            p.amount as amount,
//            p.payment_date as paymentDate,
//            'LEASE' as paymentType,
//            l.lease_id as leaseId,
//            t.name as tenantName,
//            s.name as spaceName,
//            CAST(NULL AS VARCHAR) as utilityType
//        FROM payment p
//        JOIN lease l ON p.lease_id = l.lease_id
//        JOIN space s ON l.space_id = s.space_id
//        JOIN branch b ON s.branch_id = b.id
//        JOIN user t ON l.tenant_user_id = t.user_id
//        WHERE p.status IN ('PAID', 'VERIFIED')
//          AND p.payment_date BETWEEN :startDate AND :endDate
//          AND (:branchId IS NULL OR b.id = :branchId)
//
//        UNION ALL
//
//        SELECT
//            b.id as branchId,
//            b.name as branchName,
//            p.payment_id as paymentId,
//            p.amount as amount,
//            p.payment_date as paymentDate,
//            'UTILITY' as paymentType,
//            NULL as leaseId,
//            t.name as tenantName,
//            s.name as spaceName,
//            u.utility_type as utilityType
//        FROM payment p
//        JOIN utility u ON p.utility_id = u.utility_id
//        JOIN space s ON u.space_id = s.space_id
//        JOIN branch b ON s.branch_id = b.id
//        JOIN lease l ON s.space_id = l.space_id AND l.status = 'ACTIVE'
//        JOIN user t ON l.tenant_user_id = t.user_id
//        WHERE p.status IN ('PAID', 'VERIFIED')
//          AND p.payment_date BETWEEN :startDate AND :endDate
//          AND (:branchId IS NULL OR b.id = :branchId)
//
//        ORDER BY branchName, paymentDate, paymentType
//        """, nativeQuery = true)
//    List<Object[]> findBranchIncomeData(@Param("startDate") LocalDate startDate,
//                                        @Param("endDate") LocalDate endDate,
//                                        @Param("branchId") Long branchId);
//}