package com.sein_gar_har.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStatusUpdateDTO {

    private String action;

    private ChatDTO chat;

    private UUID initiatorId;

    private UUID affectedUserId;
}