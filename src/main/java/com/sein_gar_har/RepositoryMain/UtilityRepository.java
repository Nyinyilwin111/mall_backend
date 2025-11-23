package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Utility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface UtilityRepository extends JpaRepository<Utility, Long> {

    // Find all active utilities by space ID
    @Query("SELECT u FROM Utility u WHERE u.space.spaceId = :spaceId AND u.recordStatus = 'ACTIVE'")
    List<Utility> findAllBySpaceId(@Param("spaceId") UUID spaceId);

    // Find utilities by due date range (active only)
    @Query("SELECT u FROM Utility u WHERE u.dueDate BETWEEN :startDate AND :endDate AND u.recordStatus = 'ACTIVE'")
    List<Utility> findAllByDueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find utilities by space ID and due date range (active only)
    @Query("SELECT u FROM Utility u WHERE u.space.spaceId = :spaceId AND u.dueDate BETWEEN :startDate AND :endDate AND u.recordStatus = 'ACTIVE'")
    List<Utility> findAllBySpaceIdAndDueDateBetween(
            @Param("spaceId") UUID spaceId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find utilities by utility type (active only)
    @Query("SELECT u FROM Utility u WHERE u.utilityType = :utilityType AND u.recordStatus = 'ACTIVE'")
    List<Utility> findByUtilityType(@Param("utilityType") String utilityType);

    // Find utilities by space ID and utility type (active only)
    @Query("SELECT u FROM Utility u WHERE u.space.spaceId = :spaceId AND u.utilityType = :utilityType AND u.recordStatus = 'ACTIVE'")
    List<Utility> findBySpaceIdAndUtilityType(@Param("spaceId") UUID spaceId, @Param("utilityType") String utilityType);

    // Find utilities with readings (both previous and current readings exist)
    @Query("SELECT u FROM Utility u WHERE u.previousReading IS NOT NULL AND u.currentReading IS NOT NULL AND u.recordStatus = 'ACTIVE'")
    List<Utility> findUtilitiesWithReadings();

    // Find utilities by space ID with readings
    @Query("SELECT u FROM Utility u WHERE u.space.spaceId = :spaceId AND u.previousReading IS NOT NULL AND u.currentReading IS NOT NULL AND u.recordStatus = 'ACTIVE'")
    List<Utility> findUtilitiesWithReadingsBySpaceId(@Param("spaceId") UUID spaceId);

    // Find latest utility by space ID and utility type (for getting previous reading)
    @Query("SELECT u FROM Utility u WHERE u.space.spaceId = :spaceId AND u.utilityType = :utilityType AND u.recordStatus = 'ACTIVE' ORDER BY u.dueDate DESC LIMIT 1")
    Utility findLatestUtilityBySpaceIdAndType(@Param("spaceId") UUID spaceId, @Param("utilityType") String utilityType);
}