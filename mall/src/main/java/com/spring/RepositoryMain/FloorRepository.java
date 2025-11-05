package com.spring.RepositoryMain;

import com.spring.Entity.Branch;
import com.spring.Entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FloorRepository extends JpaRepository<Floor, UUID> {
    Optional<Floor> findByLevelAndBranch(String level, Branch branch);
    List<Floor> findByBranch(Branch branch);
}
