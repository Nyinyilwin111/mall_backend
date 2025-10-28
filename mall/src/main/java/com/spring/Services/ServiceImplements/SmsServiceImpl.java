package com.spring.Services.ServiceImplements;


import com.spring.Services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@EnableScheduling
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Scheduled(fixedRate = 10000)
    public void sendPush() {
//        String message = "Server push message at " + LocalDateTime.now();
//        messagingTemplate.convertAndSend("/topic/push", message);
//        System.out.println("Sent push message: " + message);
    }
}