// AuditLogRepository.java
package com.sein_gar_har.RepositoryAudit;

import com.sein_gar_har.auditEntity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // Make sure this method exists and works correctly
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    List<AuditLog> findAllByOrderByTimestampDesc();

    // General audit queries
    List<AuditLog> findByPerformedByAndTimestampBetweenOrderByTimestampDesc(
            String performedBy, LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByActionAndTimestampBetweenOrderByTimestampDesc(
            String action, LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByTableAffectedAndTimestampBetweenOrderByTimestampDesc(
            String table, LocalDateTime start, LocalDateTime end);

    // User-specific activity reports
    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :username " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findUserActivityReport(@Param("username") String username,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Login activity reports
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN' " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findLoginActivityReport(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN' " +
            "AND a.performedBy = :username " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findUserLoginHistory(@Param("username") String username,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Role change reports
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(a.tableAffected = 'User' AND a.newValues LIKE '%roles%') " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findRoleChangeAudit(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :adminUser " +
            "AND a.tableAffected = 'User' " +
            "AND a.newValues LIKE '%roles%' " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findAdminRoleChanges(@Param("adminUser") String adminUser,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Add the missing method for comprehensive report
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);
}