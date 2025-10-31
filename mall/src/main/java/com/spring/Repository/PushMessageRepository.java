package com.spring.Repository;

import com.spring.Entity.Branch;
import com.spring.Entity.PushMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PushMessageRepository extends JpaRepository<PushMessage, UUID> {

    List<PushMessage> findBySentToAllTrueOrBranch(Branch branch);
}
