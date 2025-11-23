package com.sein_gar_har.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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