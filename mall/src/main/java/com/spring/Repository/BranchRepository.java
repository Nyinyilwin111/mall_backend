package com.spring.Repository;

import com.spring.Entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

}
