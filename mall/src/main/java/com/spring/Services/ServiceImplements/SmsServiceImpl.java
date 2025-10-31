package com.spring.Services.ServiceImplements;


import com.spring.DTO.request.GetPushMessageDto;
import com.spring.Entity.PushMessage;
import com.spring.Entity.User;
import com.spring.Repository.PushMessageRepository;
import com.spring.Repository.UserRepository;
import com.spring.Services.PushMessageService;
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

    @Override
    @Scheduled(fixedRate = 10000)
    public void sendPush() {
//        String message = "Server push message at " + LocalDateTime.now();
//        messagingTemplate.convertAndSend("/topic/push", message);
//        System.out.println("Sent push message: " + message);
    }

    @Override
    public List<GetPushMessageDto> getMessagesForUser(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        System.out.println("this user of branch name is : "+optionalUser.get().getBranch());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();

        // Get user's branch
        if (user.getBranch() == null) {
            // If user has no branch, return only global messages (sentToAll = true)
            return pushMessageRepository.findBySentToAllTrueOrBranch(null)
                    .stream()
                    .filter(PushMessage::isSentToAll)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        // Otherwise, get messages for their branch or sent to all
        List<PushMessage> messages = pushMessageRepository.findBySentToAllTrueOrBranch(user.getBranch());
        for (PushMessage mes : messages){
            System.out.println("this branch of message : "+mes);
        }
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void save(PushMessage message) {
        pushMessageRepository.save(message);
    }

    private GetPushMessageDto convertToDTO(PushMessage msg) {
        return new GetPushMessageDto(
                msg.getId(),
                msg.getMessage(),
                msg.getDateTime(),
                msg.isSentToAll(),
                msg.getBranch() != null ? msg.getBranch().getName() : null,
                msg.getCreatedUserId() != null ? msg.getCreatedUserId().getFullName() : "System"
        );
    }
}