//package com.spring.Config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.stereotype.Component;
//
//import java.security.Principal;
//
//@Component
//public class AuthChannelInterceptor implements ChannelInterceptor {
//
//    @Autowired
//    private JwtTokenProvider tokenProvider;
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            String token = extractToken(accessor);
//            if (token != null && tokenProvider.validateToken(token)) {
//                String username = tokenProvider.getUsernameFromToken(token);
//                accessor.setUser(new Principal() {
//                    @Override
//                    public String getName() {
//                        return username;
//                    }
//                });
//            }
//        }
//        return message;
//    }
//
//    private String extractToken(StompHeaderAccessor accessor) {
//        String token = accessor.getFirstNativeHeader("Authorization");
//        if (token != null && token.startsWith("Bearer ")) {
//            return token.substring(7);
//        }
//        return null;
//    }
//}