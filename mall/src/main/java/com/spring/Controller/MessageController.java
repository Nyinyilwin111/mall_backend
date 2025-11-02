package com.spring.Controller;

import com.spring.Config.JwtConstants;
import com.spring.DTO.response.ApiResponseDTO;
import com.spring.DTO.response.MessageDTO;
import com.spring.Entity.Message;
import com.spring.Entity.User;
import com.spring.Exceptions.ChatException;
import com.spring.Exceptions.MessageException;
import com.spring.Exceptions.UserException;
import com.spring.Services.MessageService;
import com.spring.Services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @PostMapping("/create")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam UUID chatId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt
    ) throws ChatException, UserException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        System.out.println("----Reached Controller----");
        User user = userService.findUserByProfile(jwt);
        System.out.println("----User Found: " + user.getEmail() + "----");

        if (file != null && !file.isEmpty()) {
            long sizeInBytes = file.getSize();
            System.out.println("File size in bytes: " + sizeInBytes);

            // Optional: reject if size > 5MB
            long maxSize = 200 * 1024 * 1024;
            if (sizeInBytes > maxSize) {
                throw new ChatException("File size exceeds maximum 5MB");
            }
        }
        // Send message
        Message message = messageService.sendMessageWithFile(chatId, content, file, user.getId());
        System.out.println("----Message Saved: " + message.getId() + "----");

        return ResponseEntity.ok(MessageDTO.fromMessage(message));
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageDTO>> getChatMessages(@PathVariable UUID chatId,
                                                            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws ChatException, UserException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        List<Message> messages = messageService.getChatMessages(chatId, user);

        return new ResponseEntity<>(MessageDTO.fromMessages(messages), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> deleteMessage(@PathVariable UUID id,
                                                        @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException, MessageException {

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // remove "Bearer "
        }

        User user = userService.findUserByProfile(jwt);
        messageService.deleteMessageById(id, user);
        log.info("User {} deleted message: {}", user.getEmail(), id);

        ApiResponseDTO res = ApiResponseDTO.builder()
                .message("Message deleted successfully")
                .status(true)
                .build();

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
