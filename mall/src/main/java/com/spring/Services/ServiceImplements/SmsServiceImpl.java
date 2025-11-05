package com.spring.Services.ServiceImplements;

import com.spring.DTO.request.GetPushMessageDto;
import com.spring.Entity.PushMessage;
import com.spring.Entity.User;
import com.spring.RepositoryMain.PushMessageRepository;
import com.spring.RepositoryMain.UserRepository;
import com.spring.Services.PushMessageService;
import com.spring.Util.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class SmsServiceImpl implements PushMessageService {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    PushMessageRepository pushMessageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageConverter messageConverter; // Inject the converter

    @Override
    @Scheduled(fixedRate = 10000)
    public void sendPush() {
        // Your existing scheduled push logic
    }

    @Override
    public List<GetPushMessageDto> getMessagesForUser(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();
        List<PushMessage> messages = pushMessageRepository.findMessagesForUser(user);

        System.out.println("Found " + messages.size() + " messages for user: " + user.getEmail());

        // Use the converter to avoid serialization issues
        return messages.stream()
                .map(messageConverter::convertToDto)
                .collect(Collectors.toList());
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
}