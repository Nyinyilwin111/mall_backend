package com.spring.Util;

import com.spring.DTO.request.GetPushMessageDto;
import com.spring.Entity.PushMessage;
import com.spring.Entity.User;
import com.spring.Repository.UserRepository;
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
            dto.setSenderName(sender.map(User::getFullName).orElse("System"));
        } else {
            dto.setSenderName("System");
        }

        return dto;
    }
}