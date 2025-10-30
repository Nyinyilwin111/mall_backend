package com.spring.Repository;


import com.spring.Entity.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpaceTypeRepository extends JpaRepository<SpaceType, UUID> {
}