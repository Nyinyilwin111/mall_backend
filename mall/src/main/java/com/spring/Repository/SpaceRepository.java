package com.spring.Repository;



import com.spring.Entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<Space, UUID> {
    List<Space> findByFloorFloorId(Integer floorId);
    List<Space> findBySpaceTypeSpaceTypeId(UUID spaceTypeId);
}