//package com.sein_gar_har.controller;
//
//import com.sein_gar_har.Services.MessageService;
//import com.sein_gar_har.Services.UserService;
//import com.sein_gar_har.config.JwtConstants;
//import com.sein_gar_har.dto.request.SendMessageRequestDTO;
//import com.sein_gar_har.dto.response.ApiResponseDTO;
//import com.sein_gar_har.dto.response.MessageDTO;
//import com.sein_gar_har.entity.Message;
//import com.sein_gar_har.entity.User;
//import com.sein_gar_har.exception.ChatException;
//import com.sein_gar_har.exception.MessageException;
//import com.sein_gar_har.exception.UserException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/messages")
//public class MessageController {
//
//    private final UserService userService;
//    private final MessageService messageService;
//
//    @PostMapping("/create")
//    public ResponseEntity<MessageDTO> sendMessage(@RequestBody SendMessageRequestDTO req,
//                                                  @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
//            throws ChatException, UserException {
//
//        User user = userService.findUserByProfile(jwt);
//        Message message = messageService.sendMessage(req, user.getId());
//        log.info("User {} sent message: {}", user.getEmail(), message.getId());
//
//        return new ResponseEntity<>(MessageDTO.fromMessage(message), HttpStatus.OK);
//    }
//
//    @GetMapping("/chat/{chatId}")
//    public ResponseEntity<List<MessageDTO>> getChatMessages(@PathVariable UUID chatId,
//                                                            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
//            throws ChatException, UserException {
//
//        User user = userService.findUserByProfile(jwt);
//        List<Message> messages = messageService.getChatMessages(chatId, user);
//
//        return new ResponseEntity<>(MessageDTO.fromMessages(messages), HttpStatus.OK);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponseDTO> deleteMessage(@PathVariable UUID id,
//                                                        @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
//            throws UserException, MessageException {
//
//        User user = userService.findUserByProfile(jwt);
//        messageService.deleteMessageById(id, user);
//        log.info("User {} deleted message: {}", user.getEmail(), id);
//
//        ApiResponseDTO res = ApiResponseDTO.builder()
//                .message("Message deleted successfully")
//                .status(true)
//                .build();
//
//        return new ResponseEntity<>(res, HttpStatus.OK);
//    }
//
//}


package com.sein_gar_har.controller;

import com.sein_gar_har.Services.MessageService;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.dto.request.SendMessageRequestDTO;
import com.sein_gar_har.dto.response.ApiResponseDTO;
import com.sein_gar_har.dto.response.MessageDTO;
import com.sein_gar_har.entity.Message;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.ChatException;
import com.sein_gar_har.exception.MessageException;
import com.sein_gar_har.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    private final UserService userService;
    private final MessageService messageService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam("chatId") UUID chatId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "voiceFile", required = false) MultipartFile voiceFile,
            @RequestParam(value = "voiceDuration", required = false) Integer voiceDuration,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws ChatException, UserException {

        log.info("Received message - ChatId: {}, Content: {}, HasFile: {}, HasVoice: {}",
                chatId, content, file != null && !file.isEmpty(), voiceFile != null && !voiceFile.isEmpty());

        User user = userService.findUserByProfile(jwt);

        SendMessageRequestDTO request = new SendMessageRequestDTO(
                chatId, content, file, voiceFile, voiceDuration
        );

        Message message = messageService.sendMessage(request, user.getId());
        log.info("User {} sent message: {}", user.getEmail(), message.getId());

        return new ResponseEntity<>(MessageDTO.fromMessage(message), HttpStatus.OK);
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageDTO>> getChatMessages(@PathVariable UUID chatId,
                                                            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws ChatException, UserException {

        User user = userService.findUserByProfile(jwt);
        List<Message> messages = messageService.getChatMessages(chatId, user);

        return new ResponseEntity<>(MessageDTO.fromMessages(messages), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> deleteMessage(@PathVariable UUID id,
                                                        @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt)
            throws UserException, MessageException {

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