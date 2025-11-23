package com.sein_gar_har.websocket;

import com.sein_gar_har.auditEntity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
// AuditStompSender.java - UPDATED
@Component
@RequiredArgsConstructor
public class AuditStompSender {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(AuditLog log) {
        try {
            messagingTemplate.convertAndSend("/topic/auditLogs", log);
            System.out.println("📢 WebSocket broadcast sent for log: " + log.getLogId());
        } catch (Exception e) {
            System.err.println("❌ WebSocket broadcast failed: " + e.getMessage());
        }
    }
}