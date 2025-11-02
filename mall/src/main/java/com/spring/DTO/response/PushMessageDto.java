package com.spring.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageDto {
    private UUID id;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private boolean read;
    private boolean sentToAll;
    private String branchName;
    private String senderName;

    // Optional: Add formatted date for frontend
    public String getFormattedDate() {
        return createdAt != null ? createdAt.toString() : "";
    }
}