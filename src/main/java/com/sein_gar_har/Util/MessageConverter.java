package com.sein_gar_har.Util;

import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.dto.request.GetPushMessageDto;
import com.sein_gar_har.entity.PushMessage;
import com.sein_gar_har.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessageConverter {

    @Autowired
    private UserRepository userRepository;

    public GetPushMessageDto convertToDto(PushMessage message) {
        if (message == null) {
            return null;
        }

        GetPushMessageDto dto = new GetPushMessageDto();
        dto.setId(message.getId());
        dto.setMessage(message.getMessage());
        dto.setDateTime(message.getDateTime());
        dto.setSentToAll(message.isSentToAll());
        dto.setReadby(message.isReadby());

        // Set branch name safely
        if (message.getBranch() != null) {
            dto.setBranchName(message.getBranch().getName());
        }

        // Set sender name safely - fetch user separately to avoid lazy loading issues
        if (message.getCreatedUserId() != null) {
            Optional<User> sender = userRepository.findById(message.getCreatedUserId().getId());
            dto.setCreatedUserName(sender.map(User::getFullName).orElse("System"));
        } else {
            dto.setCreatedUserName("System");
        }

        return dto;
    }
}