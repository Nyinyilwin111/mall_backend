package com.spring.Services;

import com.spring.DTO.request.SendMessageRequestDTO;
import com.spring.Entity.Message;
import com.spring.Entity.User;
import com.spring.Exceptions.ChatException;
import com.spring.Exceptions.MessageException;
import com.spring.Exceptions.UserException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message sendMessage(SendMessageRequestDTO req, UUID userId) throws ChatException, UserException;

    List<Message> getChatMessages(UUID chatId, User reqUser) throws ChatException, UserException;

    Message findMessageById(UUID messageId) throws MessageException;

    void deleteMessageById(UUID messageId, User reqUser) throws MessageException, UserException;

    Message sendMessageWithFile(UUID chatId, String content, MultipartFile file, UUID userId)
            throws ChatException, UserException;

    void deleteAllMessagesByChatId(UUID chatId) throws ChatException;
}