package com.spring.DTO.request;
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
    private String message;
    private LocalDateTime dateTime;
    private boolean sentToAll;
    private String branchName;
    private String senderName;
}
