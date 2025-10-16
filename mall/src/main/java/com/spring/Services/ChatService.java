package com.spring.Services;

import com.spring.DTO.request.GroupChatRequestDTO;
import com.spring.Entity.Chat;
import com.spring.Entity.User;
import com.spring.Exceptions.ChatException;
import com.spring.Exceptions.UserException;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    Chat createChat(User reqUser, UUID userId2) throws UserException;

    Chat findChatById(UUID id) throws ChatException;

    List<Chat> findAllByUserId(UUID userId) throws UserException;

    Chat createGroup(GroupChatRequestDTO req, User reqUser) throws UserException;

    Chat addUserToGroup(UUID userId, UUID chatId, User reqUser) throws ChatException, UserException;

    Chat renameGroup(UUID chatId, String groupName, User reqUser) throws ChatException, UserException;

    Chat removeFromGroup(UUID chatId, UUID userId, User reqUser) throws ChatException, UserException;
    void deleteChat(UUID chatId, UUID userId) throws ChatException, UserException;

    Chat markAsRead(UUID chatId, User reqUser) throws ChatException, UserException;

}
