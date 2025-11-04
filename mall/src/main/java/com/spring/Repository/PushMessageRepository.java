package com.spring.Repository;

import com.spring.Entity.Branch;
import com.spring.Entity.PushMessage;
import com.spring.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PushMessageRepository extends JpaRepository<PushMessage, UUID> {

    List<PushMessage> findBySentToAllTrueOrBranch(Branch branch);

    @Query("SELECT pm FROM PushMessage pm WHERE " +
            "pm.recipientUser = :user OR " +
            "pm.recipientUser IS NULL OR " +
            "pm.sentToAll = true ")
    List<PushMessage> findMessagesForUser(@Param("user") User user);

    Long countByRecipientUserIdAndReadbyFalse(UUID recipientUserId);

}
