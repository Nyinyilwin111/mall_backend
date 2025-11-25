//package com.sein_gar_har.dto.response;
//
//import com.sein_gar_har.entity.Message;
//import lombok.Builder;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Builder
//public record MessageDTO(UUID id, String content, LocalDateTime timeStamp, UserDTO user, Set<UUID> readBy) {
//
//    public static MessageDTO fromMessage(Message message) {
//        if (Objects.isNull(message)) return null;
//        return MessageDTO.builder()
//                .id(message.getId())
//                .content(message.getContent())
//                .timeStamp(message.getTimeStamp())
//                .user(UserDTO.fromUser(message.getUser()))
//                .readBy(new HashSet<>(message.getReadBy()))
//                .build();
//    }
//
//    public static List<MessageDTO> fromMessages(Collection<Message> messages) {
//        if (Objects.isNull(messages)) return List.of();
//        return messages.stream()
//                .map(MessageDTO::fromMessage)
//                .toList();
//    }
//
//}


package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Message;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.*;

@Builder
public record MessageDTO(
        UUID id,
        String content,
        LocalDateTime timeStamp,
        UserDTO user,
        Set<UUID> readBy,
        // File fields
        String filePath,
        String fileName,
        String fileType,
        Long fileSize,
        String mimeType,
        // Voice message fields
        String voiceFilePath,
        String voiceFileName,
        Integer voiceDuration
) {

    public static MessageDTO fromMessage(Message message) {
        if (Objects.isNull(message)) return null;
        return MessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .timeStamp(message.getTimeStamp())
                .user(UserDTO.fromUser(message.getUser()))
                .readBy(new HashSet<>(message.getReadBy()))
                // File fields
                .filePath(message.getFilePath())
                .fileName(message.getFileName())
                .fileType(message.getFileType())
                .fileSize(message.getFileSize())
                .mimeType(message.getMimeType())
                // Voice fields
                .voiceFilePath(message.getVoiceFilePath())
                .voiceFileName(message.getVoiceFileName())
                .voiceDuration(message.getVoiceDuration())
                .build();
    }

    public static List<MessageDTO> fromMessages(Collection<Message> messages) {
        if (Objects.isNull(messages)) return List.of();
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .toList();
    }
}