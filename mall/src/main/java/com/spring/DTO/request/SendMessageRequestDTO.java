package com.spring.DTO.request;

import java.util.UUID;

public record SendMessageRequestDTO(UUID chatId, String content) {
}
