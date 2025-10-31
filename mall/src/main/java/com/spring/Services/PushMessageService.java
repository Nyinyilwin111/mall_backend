package com.spring.Services;

import com.spring.DTO.request.GetPushMessageDto;
import com.spring.Entity.PushMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PushMessageService {
    void sendPush();

    List<GetPushMessageDto> getMessagesForUser(UUID userId);


    void save(PushMessage message);
}
