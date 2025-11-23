package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Integer> {
    List<Floor> findByBranchBranchId(Integer branchBranchId);
}