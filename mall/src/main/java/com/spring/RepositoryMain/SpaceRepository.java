package com.spring.RepositoryMain;

import com.spring.Entity.Branch;
import com.spring.Entity.Floor;
import com.spring.Entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceRepository extends JpaRepository<Space, UUID> {
    Optional<Space> findByLocationAndFloor(String location, Floor floor);
    List<Space> findByFloor(Floor floor);
    List<Space> findByFloor_Branch(Branch branch);
}