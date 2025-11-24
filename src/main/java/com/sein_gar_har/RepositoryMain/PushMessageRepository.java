package com.sein_gar_har.RepositoryMain;

import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.PushMessage;
import com.sein_gar_har.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PushMessageRepository extends JpaRepository<PushMessage, UUID> {

    Long countByRecipientUserIdAndReadbyFalse(UUID userId);

    // 🔴 CRITICAL: Add this method to fetch messages with branch relationship
    @Query("SELECT pm FROM PushMessage pm LEFT JOIN FETCH pm.branch WHERE pm.recipientUser.id = :userId ORDER BY pm.dateTime DESC")
    List<PushMessage> findByRecipientUserIdWithBranch(@Param("userId") UUID userId);

    List<PushMessage> findByRecipientUser_IdAndReadby(UUID userId, boolean readby);

    List<PushMessage> findByRecipientUser_Id(UUID userId);
}
