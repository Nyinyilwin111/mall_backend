package com.sein_gar_har.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class GetPushMessageDto {
    private UUID id;
    private String message;
    private LocalDateTime dateTime;
    private boolean readby;
    private boolean sentToAll;

    // Lease notification fields
    private String type;
    private String tenantId;
    private String branchID;
    private String spaceId;
    private String spaceCode;
    private String tenantName;
    private String rentAmount;
    private String createdUserName;
    private String branchName;
}