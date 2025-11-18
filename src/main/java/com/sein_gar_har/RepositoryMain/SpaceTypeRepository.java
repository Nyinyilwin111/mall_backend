package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpaceTypeRepository extends JpaRepository<SpaceType, UUID> {
}