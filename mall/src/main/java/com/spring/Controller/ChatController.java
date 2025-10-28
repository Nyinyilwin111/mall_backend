package com.spring.Controller;

import com.spring.Config.JwtConstants;
import com.spring.DTO.request.GroupChatRequestDTO;
import com.spring.DTO.response.ApiResponseDTO;
import com.spring.DTO.response.ChatDTO;
import com.spring.Entity.Chat;
import com.spring.Entity.User;
import com.spring.Exceptions.ChatException;
import com.spring.Exceptions.UserException;
import com.spring.Services.ChatService;
import com.spring.Services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    UserService userService;

    @Autowired
    ChatService chatService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @PostMapping("/single")
    public ResponseEntity<ChatDTO> createSingleChat(@RequestBody UUID userId,
                                                    @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        Chat chat = chatService.createChat(user, userId);
        log.info("User {} created single chat: {}", user.getEmail(), chat.getId());

        return new ResponseEntity<>(ChatDTO.fromChat(chat), HttpStatus.OK);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDTO> createGroupChat(@RequestBody GroupChatRequestDTO req,
                                                   @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }
        System.out.println("reach in backend--------!");
        User user = userService.findUserByProfile(jwt);
        Chat chat = chatService.createGroup(req, user);
        log.info("User {} created group chat: {}", user.getEmail(), chat.getId());

        return new ResponseEntity<>(ChatDTO.fromChat(chat), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatDTO> findChatById(@PathVariable("id") UUID id)
            throws ChatException {

        Chat chat = chatService.findChatById(id);

        return new ResponseEntity<>(ChatDTO.fromChat(chat), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ChatDTO>> findAllChatsByUserId(@RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        List<Chat> chats = chatService.findAllByUserId(user.getId());

        return new ResponseEntity<>(ChatDTO.fromChats(chats), HttpStatus.OK);
    }

    @PutMapping("/{chatId}/add/{userId}")
    public ResponseEntity<ChatDTO> addUserToGroup(@PathVariable UUID chatId, @PathVariable UUID userId,
                                                  @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException, ChatException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        Chat chat = chatService.addUserToGroup(userId, chatId, user);
        log.info("User {} added user {} to group chat: {}", user.getEmail(), userId, chat.getId());

        return new ResponseEntity<>(ChatDTO.fromChat(chat), HttpStatus.OK);
    }

    @PutMapping("/{chatId}/remove/{userId}")
    public ResponseEntity<ChatDTO> removeUserFromGroup(
            @PathVariable UUID chatId,
            @PathVariable UUID userId,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException, ChatException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User currentUser = userService.findUserByProfile(jwt);
        Chat chat = chatService.removeFromGroup(chatId, userId, currentUser);
        ChatDTO updatedChat = ChatDTO.fromChat(chat);

        log.info("User {} removed user {} from group chat: {}", currentUser.getEmail(), userId, chat.getId());

        // Notify all remaining members about group update
        for (User member : chat.getUsers()) {
            messagingTemplate.convertAndSend("/topic/" + member.getId(), updatedChat);
        }

        // Notify the removed user separately
        messagingTemplate.convertAndSend("/topic/chat-removed/" + userId, updatedChat);

        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }


    @PutMapping("/{chatId}/markAsRead")
    public ResponseEntity<ChatDTO> markAsRead(@PathVariable UUID chatId,
                                              @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException, ChatException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        Chat chat = chatService.markAsRead(chatId, user);
        log.info("Chat {} marked as read for user: {}", chatId, user.getEmail());

        return new ResponseEntity<>(ChatDTO.fromChat(chat), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDTO> deleteChat(@PathVariable UUID id,
                                                     @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException, ChatException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        System.out.println("chart deleting");
        chatService.deleteChat(id, user.getId());
        log.info("User {} deleted chat: {}", user.getEmail(), id);

        ApiResponseDTO res = ApiResponseDTO.builder()
                .message("Chat deleted successfully")
                .status(true)
                .build();

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
