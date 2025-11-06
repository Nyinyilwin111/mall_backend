package com.spring.DTO.response;

import com.spring.Entity.Message;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.*;

@Builder
public record MessageDTO(
        UUID id,
        String content,
        String filePath,
        LocalDateTime timeStamp,
        UserDTO user,
        Set<UUID> readBy
) {
    public static MessageDTO fromMessage(Message message) {
        if (Objects.isNull(message)) return null;
        return MessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .filePath(message.getFilePath())
                .timeStamp(message.getTimeStamp())
                .user(UserDTO.fromUser(message.getUser()))
                .readBy(new HashSet<>(message.getReadBy()))
                .build();
    }

    public static List<MessageDTO> fromMessages(Collection<Message> messages) {
        if (Objects.isNull(messages)) return List.of();
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .toList();
    }
}
