package com.spring.Repository;

import com.spring.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

//    List<Message> findByChat_Id(UUID chatId, UUID userId);

    @Query("SELECT m FROM Message m " +
            "JOIN m.chat c " +
            "JOIN c.users u " +
            "WHERE c.id = :chatId AND u.id = :userId")
    List<Message> findMessagesByChatAndUser(@Param("chatId") UUID chatId, @Param("userId") UUID userId);
}