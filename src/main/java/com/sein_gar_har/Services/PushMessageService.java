package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.GetPushMessageDto;
import com.sein_gar_har.entity.PushMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PushMessageService {
    //    void sendPush();
    List<GetPushMessageDto> getMessagesForUser(UUID userId);
    //    PushMessage createMessage(PushMessageDto requestDto);
    PushMessage save(PushMessage message);
    PushMessage markAsRead(UUID messageUuid);

    Long getUnreadCount(UUID userId);

    PushMessage findById(UUID messageUuid);
}
