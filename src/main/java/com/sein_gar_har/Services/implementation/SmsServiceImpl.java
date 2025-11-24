package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.PushMessageRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.PushMessageService;
import com.sein_gar_har.Util.MessageConverter;
import com.sein_gar_har.dto.request.GetPushMessageDto;
import com.sein_gar_har.entity.PushMessage;
import com.sein_gar_har.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class SmsServiceImpl implements PushMessageService {

    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    PushMessageRepository pushMessageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageConverter messageConverter; // Inject the converter

    @Override
    public List<GetPushMessageDto> getMessagesForUser(UUID userId) {
        // 🔴 IMPORTANT: Make sure to fetch branch relationship eagerly
        List<PushMessage> messages = pushMessageRepository.findByRecipientUserIdWithBranch(userId);

        System.out.println("🔍 Fetching messages for user: " + userId);
        System.out.println("📋 Found " + messages.size() + " messages");

        List<GetPushMessageDto> dtos = messages.stream().map(this::convertToDto).collect(Collectors.toList());

        // Debug output
        dtos.forEach(dto -> {
            System.out.println("📦 Message ID: " + dto.getId() +
                    " | BranchID: " + dto.getBranchID() +
                    " | TenantID: " + dto.getTenantId() +
                    " | Type: " + dto.getType());
        });

        return dtos;
    }

    private GetPushMessageDto convertToDto(PushMessage message) {
        GetPushMessageDto dto = new GetPushMessageDto();
        dto.setId(message.getId());
        dto.setMessage(message.getMessage());
        dto.setDateTime(message.getDateTime());
        dto.setReadby(message.isReadby());
        dto.setSentToAll(message.isSentToAll());

        // Map lease notification fields
        dto.setType(message.getType());
        dto.setTenantId(message.getTenantId());
        dto.setSpaceId(message.getSpaceId());
        dto.setSpaceCode(message.getSpaceCode());
        dto.setTenantName(message.getTenantName());
        dto.setRentAmount(message.getRentAmount());
        dto.setCreatedUserName(message.getCreatedUserName());

        // 🔴 CRITICAL: Extract branch information from relationship
        if (message.getBranch() != null) {
            dto.setBranchID(message.getBranch().getId().toString());
            dto.setBranchName(message.getBranch().getName());
        } else {
            dto.setBranchID(null);
            dto.setBranchName(null);
        }

        System.out.println("🔄 Converted message: " + dto.getId() +
                " | BranchID: " + dto.getBranchID() +
                " | TenantID: " + dto.getTenantId() +
                " | Has Branch: " + (message.getBranch() != null));

        return dto;
    }

    @Override
    public PushMessage save(PushMessage message) {
        return pushMessageRepository.save(message);
    }

    @Override
    public PushMessage markAsRead(UUID messageUuid) {
        Optional<PushMessage> optionalMessage = pushMessageRepository.findById(messageUuid);
        if (optionalMessage.isPresent()) {
            PushMessage message = optionalMessage.get();
            message.setReadby(true);
            return pushMessageRepository.save(message);
        }else {
            return null;
        }

    }

    @Override
    public Long getUnreadCount(UUID userId) {
        return pushMessageRepository.countByRecipientUserIdAndReadbyFalse(userId);
    }

    @Override
    public PushMessage findById(UUID messageUuid) {
        return pushMessageRepository.findById(messageUuid).orElse(null);
    }

    @Override
    public void deleteMessage(String messageId) {
        try {
            pushMessageRepository.deleteById(UUID.fromString(messageId));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting message with id: " + messageId, e);
        }
    }

    @Override
    public int deleteAllUserMessages(String userId) {
        try {
            UUID id = UUID.fromString(userId);
            List<PushMessage> userMessages = pushMessageRepository.findByRecipientUser_Id(id);
            int count = userMessages.size();
            pushMessageRepository.deleteAll(userMessages);
            return count;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting all messages for user: " + userId, e);
        }
    }

    @Override
    public int deleteReadMessages(String userId) {
        try {
            UUID id = UUID.fromString(userId);
            List<PushMessage> readMessages = pushMessageRepository.findByRecipientUser_IdAndReadby(id, true);
            int count = readMessages.size();
            pushMessageRepository.deleteAll(readMessages);
            return count;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting read messages for user: " + userId, e);
        }
    }

}