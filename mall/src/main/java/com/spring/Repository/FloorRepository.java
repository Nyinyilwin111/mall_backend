package com.spring.Repository;



import com.spring.Entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Integer> {
    List<Floor> findByBranchBranchId(Integer branchBranchId);
}