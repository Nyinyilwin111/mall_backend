package com.sein_gar_har.dto.request;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public record SendMessageRequestDTO(
        UUID chatId,
        String content,
        MultipartFile file,
        MultipartFile voiceFile, // For voice messages
        Integer voiceDuration // Voice duration in seconds
) {}