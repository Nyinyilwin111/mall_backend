// SpaceRepository.java
package com.spring.Repository;

import com.spring.Entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
}