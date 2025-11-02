package com.spring.DTO.request;

import com.spring.Entity.Branch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPushMessageDto {
    private UUID id;
    private String message;      // This will be the body/content
    private LocalDateTime dateTime;
    private boolean sentToAll;
    private String branchName;
    private String senderName;
    private Branch branch;
    private boolean readby = false;

    // Add a method to get title from message for frontend
    public String getTitle() {
        if (message == null || message.trim().isEmpty()) {
            return "Notification";
        }

        // Extract title from message (first 30 chars or first sentence)
        if (message.length() <= 30) {
            return message;
        }

        int sentenceEnd = message.indexOf('.');
        if (sentenceEnd > 0 && sentenceEnd <= 50) {
            return message.substring(0, sentenceEnd + 1);
        }

        return message.substring(0, 30) + "...";
    }
}