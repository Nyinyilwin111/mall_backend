//package com.sein_gar_har.Services.implementation;
//
//import com.sein_gar_har.RepositoryMain.ChatRepository;
//import com.sein_gar_har.Services.ChatService;
//import com.sein_gar_har.Services.UserService;
//import com.sein_gar_har.dto.request.GroupChatRequestDTO;
//import com.sein_gar_har.entity.Chat;
//import com.sein_gar_har.entity.User;
//import com.sein_gar_har.exception.ChatException;
//import com.sein_gar_har.exception.UserException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class ChatServiceImpl implements ChatService {
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    ChatRepository chatRepository;
//
//    @Override
//    public Chat createChat(User reqUser, UUID userId2) throws UserException {
//
//        User user2 = userService.findUserById(userId2);
//
//        Optional<Chat> existingChatOptional = chatRepository.findSingleChatByUsers(user2, reqUser);
//
//        if (existingChatOptional.isPresent()) {
//            return existingChatOptional.get();
//        }
//
//        Chat chat = Chat.builder()
//                .createdBy(reqUser)
//                .users(new HashSet<>(Set.of(reqUser, user2)))
//                .isGroup(false)
//                .build();
//
//        return chatRepository.save(chat);
//    }
//
//    @Override
//    public Chat findChatById(UUID id) throws ChatException {
//
//        Optional<Chat> chatOptional = chatRepository.findById(id);
//
//        if (chatOptional.isPresent()) {
//            return chatOptional.get();
//        }
//
//        throw new ChatException("No chat found with id " + id);
//    }
//
//    @Override
//    public List<Chat> findAllByUserId(UUID userId) throws UserException {
//
//        User user = userService.findUserById(userId);
//
//        return chatRepository.findChatByUserId(user.getId()).stream()
//                .sorted((chat1, chat2) -> {
//                    if (chat1.getMessages().isEmpty() && chat2.getMessages().isEmpty()) {
//                        return 0;
//                    } else if (chat1.getMessages().isEmpty()) {
//                        return 1;
//                    } else if (chat2.getMessages().isEmpty()) {
//                        return -1;
//                    }
//                    LocalDateTime timeStamp1 = chat1.getMessages().get(chat1.getMessages().size() - 1).getTimeStamp();
//                    LocalDateTime timeStamp2 = chat2.getMessages().get(chat2.getMessages().size() - 1).getTimeStamp();
//                    return timeStamp2.compareTo(timeStamp1);
//                })
//                .toList();
//    }
//
//    @Override
//    public Chat createGroup(GroupChatRequestDTO req, User reqUser) throws UserException {
//
//        Chat groupChat = Chat.builder()
//                .isGroup(true)
//                .chatName(req.chatName())
//                .createdBy(reqUser)
//                .admins(new HashSet<>(Set.of(reqUser)))
//                .users(new HashSet<>())
//                .build();
//
//        for (UUID userId : req.userIds()) {
//            User userToAdd = userService.findUserById(userId);
//            groupChat.getUsers().add(userToAdd);
//        }
//
//        return chatRepository.save(groupChat);
//    }
//
//    @Override
//    public Chat addUserToGroup(UUID userId, UUID chatId, User reqUser) throws UserException, ChatException {
//
//        Chat chat = findChatById(chatId);
//        User user = userService.findUserById(userId);
//
//        if (chat.getAdmins().contains(reqUser)) {
//            chat.getUsers().add(user);
//            return chatRepository.save(chat);
//        }
//
//        throw new UserException("User doesn't have permissions to add members to group chat");
//    }
//
//    @Override
//    public Chat renameGroup(UUID chatId, String groupName, User reqUser) throws UserException, ChatException {
//
//        Chat chat = findChatById(chatId);
//
//        if (chat.getAdmins().contains(reqUser)) {
//            chat.setChatName(groupName);
//            return chatRepository.save(chat);
//        }
//
//        throw new UserException("User doesn't have permissions to rename group chat");
//    }
//
//    @Override
//    public Chat removeFromGroup(UUID chatId, UUID userId, User reqUser) throws UserException, ChatException {
//
//        Chat chat = findChatById(chatId);
//        User user = userService.findUserById(userId);
//
//        boolean isAdminOrRemoveSelf = chat.getAdmins().contains(reqUser) ||
//                (chat.getUsers().contains(reqUser) && user.getId().equals(reqUser.getId()));
//
//        if (isAdminOrRemoveSelf) {
//            chat.getUsers().remove(user);
//            return chatRepository.save(chat);
//        }
//
//        throw new UserException("User doesn't have permissions to remove users from group chat");
//    }
//
//    @Override
//    public void deleteChat(UUID chatId, UUID userId) throws UserException, ChatException {
//
//        Chat chat = findChatById(chatId);
//        User user = userService.findUserById(userId);
//
//        boolean isSingleChatOrAdmin = !chat.getIsGroup() || chat.getAdmins().contains(user);
//
//        if (isSingleChatOrAdmin) {
//            chatRepository.deleteById(chatId);
//            return;
//        }
//
//        throw new UserException("User doesn't have permissions to delete group chat");
//    }
//
//    @Override
//    public Chat markAsRead(UUID chatId, User reqUser) throws ChatException, UserException {
//
//        Chat chat = findChatById(chatId);
//
//        if (chat.getUsers().contains(reqUser)) {
//            chat.getMessages().forEach(msg -> msg.getReadBy().add(reqUser.getId()));
//
//            return chatRepository.save(chat);
//        }
//
//
//        throw new UserException("User is not related to chat");
//    }
//
//}



package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.ChatRepository;
import com.sein_gar_har.Services.ChatService;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.dto.request.GroupChatRequestDTO;
import com.sein_gar_har.dto.response.ChatDTO;
import com.sein_gar_har.dto.response.ChatStatusUpdateDTO;
import com.sein_gar_har.entity.Chat;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.ChatException;
import com.sein_gar_har.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final UserService userService;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Chat createChat(User reqUser, UUID userId2) throws UserException {
        User user2 = userService.findUserById(userId2);

        Optional<Chat> existingChatOptional = chatRepository.findSingleChatByUsers(user2, reqUser);

        if (existingChatOptional.isPresent()) {
            return existingChatOptional.get();
        }

        Chat chat = Chat.builder()
                .createdBy(reqUser)
                .users(new HashSet<>(Set.of(reqUser, user2)))
                .isGroup(false)
                .build();

        Chat saved = chatRepository.save(chat);

        // optional: notify participants if you want clients to appear instantly (e.g., new chat created)
        // notifyUsers(saved.getUsers(), new ChatStatusUpdateDTO("CHAT_CREATED", ChatDTO.fromChat(saved), reqUser.getId(), null));

        return saved;
    }

    @Override
    public Chat findChatById(UUID id) throws ChatException {
        return chatRepository.findById(id).orElseThrow(() -> new ChatException("No chat found with id " + id));
    }

    @Override
    public List<Chat> findAllByUserId(UUID userId) throws UserException {
        User user = userService.findUserById(userId);

        return chatRepository.findChatByUserId(user.getId()).stream()
                .sorted((chat1, chat2) -> {
                    if ((chat1.getMessages() == null || chat1.getMessages().isEmpty())
                            && (chat2.getMessages() == null || chat2.getMessages().isEmpty())) {
                        return 0;
                    } else if (chat1.getMessages() == null || chat1.getMessages().isEmpty()) {
                        return 1;
                    } else if (chat2.getMessages() == null || chat2.getMessages().isEmpty()) {
                        return -1;
                    }

                    LocalDateTime timeStamp1 = chat1.getMessages().get(chat1.getMessages().size() - 1).getTimeStamp();
                    LocalDateTime timeStamp2 = chat2.getMessages().get(chat2.getMessages().size() - 1).getTimeStamp();
                    return timeStamp2.compareTo(timeStamp1);
                })
                .toList();
    }

    @Override
    public Chat createGroup(GroupChatRequestDTO req, User reqUser) throws UserException {
        Chat groupChat = Chat.builder()
                .isGroup(true)
                .chatName(req.chatName())
                .createdBy(reqUser)
                .admins(new HashSet<>(Set.of(reqUser)))
                .users(new HashSet<>())
                .build();

        for (UUID userId : req.userIds()) {
            User userToAdd = userService.findUserById(userId);
            groupChat.getUsers().add(userToAdd);
        }

        Chat saved = chatRepository.save(groupChat);

        // Notify new members if needed (or the UI expects createdGroup in Redux)
        // notifyUsers(saved.getUsers(), new ChatStatusUpdateDTO("CHAT_CREATED", ChatDTO.fromChat(saved), reqUser.getId(), null));

        return saved;
    }

    @Override
    public Chat addUserToGroup(UUID userId, UUID chatId, User reqUser) throws UserException, ChatException {
        Chat chat = findChatById(chatId);
        User userToAdd = userService.findUserById(userId);

        if (chat.getAdmins().contains(reqUser)) {
            chat.getUsers().add(userToAdd);
            Chat saved = chatRepository.save(chat);

            ChatDTO chatDTO = ChatDTO.fromChat(saved);
            ChatStatusUpdateDTO update = new ChatStatusUpdateDTO("CHAT_UPDATED", chatDTO, reqUser.getId(), userId);

            notifyUsers(saved.getUsers(), update, "chat-updated");

            return saved;
        }

        throw new UserException("User doesn't have permissions to add members to group chat");
    }

    @Override
    public Chat renameGroup(UUID chatId, String groupName, User reqUser) throws UserException, ChatException {
        Chat chat = findChatById(chatId);

        // Permission check
        if (chat.getAdmins().contains(reqUser)) {
            // Update and save
            chat.setChatName(groupName);
            Chat saved = chatRepository.save(chat);

            // Build DTO payload
            ChatDTO chatDTO = ChatDTO.fromChat(saved);
            ChatStatusUpdateDTO update = new ChatStatusUpdateDTO("CHAT_UPDATED", chatDTO, reqUser.getId(), null);

            // Notify all users in this chat about the new name
            notifyUsers(saved.getUsers(), update, "chat-updated");

            return saved;
        }

        throw new UserException("User doesn't have permissions to rename group chat");
    }

    @Override
    public Chat removeFromGroup(UUID chatId, UUID userId, User reqUser) throws UserException, ChatException {
        Chat chat = findChatById(chatId);
        User user = userService.findUserById(userId);

        boolean isAdminOrRemoveSelf = chat.getAdmins().contains(reqUser) ||
                (chat.getUsers().contains(reqUser) && user.getId().equals(reqUser.getId()));

        if (isAdminOrRemoveSelf) {
            chat.getUsers().remove(user);
            Chat saved = chatRepository.save(chat);

            ChatDTO chatDTO = ChatDTO.fromChat(saved);

            // Event for removed user
            ChatStatusUpdateDTO eventForRemoved = new ChatStatusUpdateDTO("MEMBER_REMOVED", chatDTO, reqUser.getId(), userId);

            // notify the removed user via chat-removed topic
            safeSendToUser(userId, "/topic/chat-removed/", eventForRemoved);

            // notify remaining group members with "CHAT_UPDATED"
            ChatStatusUpdateDTO eventForMembers = new ChatStatusUpdateDTO("CHAT_UPDATED", chatDTO, reqUser.getId(), userId);
            notifyUsers(saved.getUsers(), eventForMembers, "chat-updated");

            return saved;
        }

        throw new UserException("User doesn't have permissions to remove users from group chat");
    }

    @Override
    public void deleteChat(UUID chatId, UUID userId) throws UserException, ChatException {
        Chat chat = findChatById(chatId);
        User user = userService.findUserById(userId);

        boolean isSingleChatOrAdmin = !chat.getIsGroup() || (chat.getAdmins() != null && chat.getAdmins().contains(user));

        if (isSingleChatOrAdmin) {
            ChatDTO chatDTO = ChatDTO.fromChat(chat);

            // Delete the chat
            chatRepository.deleteById(chatId);

            // Broadcast to participants
            ChatStatusUpdateDTO removeUpdate = new ChatStatusUpdateDTO("CHAT_REMOVED", chatDTO, user.getId(), null);

            // It's safer to copy the set of participants
            Set<User> participants = chat.getUsers() == null ? Set.of() : new HashSet<>(chat.getUsers());
            participants.forEach(u -> safeSendToUser(u.getId(), "/topic/chat-removed/", removeUpdate));

            return;
        }

        throw new UserException("User doesn't have permissions to delete group chat");
    }

    @Override
    public Chat markAsRead(UUID chatId, User reqUser) throws ChatException, UserException {
        Chat chat = findChatById(chatId);

        if (chat.getUsers().contains(reqUser)) {
            if (chat.getMessages() != null) {
                chat.getMessages().forEach(msg -> {
                    if (msg.getReadBy() == null) {
                        msg.setReadBy(new HashSet<>());
                    }
                    msg.getReadBy().add(reqUser.getId());
                });
            }

            return chatRepository.save(chat);
        }

        throw new UserException("User is not related to chat");
    }

    // Helper - send update to a set of users (using topic prefix)
    private void notifyUsers(Set<User> users, ChatStatusUpdateDTO update, String topicSuffix) {
        if (users == null) return;
        users.forEach(u -> {
            try {
                messagingTemplate.convertAndSend("/topic/" + topicSuffix + "/" + u.getId(), update);
            } catch (Exception e) {
                log.error("Failed to send {} notification to user {}: {}", topicSuffix, u.getId(), e.getMessage(), e);
            }
        });
    }

    private void safeSendToUser(UUID userId, String fullTopicPrefix, ChatStatusUpdateDTO update) {
        if (userId == null) return;
        try {
            // topicPrefix like "/topic/chat-removed/"
            // we append userId to route for compatibility with the frontend
            messagingTemplate.convertAndSend(fullTopicPrefix + userId, update);
        } catch (Exception e) {
            log.error("Failed to send direct notification to user {} via {}: {}", userId, fullTopicPrefix, e.getMessage(), e);
        }
    }
}