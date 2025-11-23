package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.SendMessageRequestDTO;
import com.sein_gar_har.entity.Message;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.ChatException;
import com.sein_gar_har.exception.MessageException;
import com.sein_gar_har.exception.UserException;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message sendMessage(SendMessageRequestDTO req, UUID userId) throws UserException, ChatException;

    List<Message> getChatMessages(UUID chatId, User reqUser) throws UserException, ChatException;

    Message findMessageById(UUID messageId) throws MessageException;

    void deleteMessageById(UUID messageId, User reqUser) throws UserException, MessageException;

}
