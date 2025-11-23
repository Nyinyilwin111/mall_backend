package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.dto.request.SpaceWithBranchDTO;
import com.sein_gar_har.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<Space, UUID> {

    List<Space> findByFloorFloorId(Integer floorId);

    List<Space> findBySpaceTypeSpaceTypeId(UUID spaceTypeId);

    Optional<Space> findBySpaceCode(String spaceCode);

    boolean existsBySpaceCode(String spaceCode);

    // Find by space code ignoring case
    @Query("SELECT s FROM Space s WHERE LOWER(s.spaceCode) = LOWER(:spaceCode)")
    Optional<Space> findBySpaceCodeIgnoreCase(@Param("spaceCode") String spaceCode);

    // Check if space code exists (excluding current space for updates)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Space s WHERE LOWER(s.spaceCode) = LOWER(:spaceCode) AND s.spaceId != :spaceId")
    boolean existsBySpaceCodeAndSpaceIdNot(@Param("spaceCode") String spaceCode, @Param("spaceId") UUID spaceId);


}