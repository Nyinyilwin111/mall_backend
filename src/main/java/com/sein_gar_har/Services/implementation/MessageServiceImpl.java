//package com.sein_gar_har.Services.implementation;
//
//import com.sein_gar_har.RepositoryMain.MessageRepository;
//import com.sein_gar_har.Services.ChatService;
//import com.sein_gar_har.Services.MessageService;
//import com.sein_gar_har.Services.UserService;
//import com.sein_gar_har.dto.request.SendMessageRequestDTO;
//import com.sein_gar_har.dto.response.MessageDTO;
//import com.sein_gar_har.entity.Chat;
//import com.sein_gar_har.entity.Message;
//import com.sein_gar_har.entity.User;
//import com.sein_gar_har.exception.ChatException;
//import com.sein_gar_har.exception.MessageException;
//import com.sein_gar_har.exception.UserException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class MessageServiceImpl implements MessageService {
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    ChatService chatService;
//
//    @Autowired
//    MessageRepository messageRepository;
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//
////    @Override
////    public Message sendMessage(SendMessageRequestDTO req, UUID userId) throws UserException, ChatException {
////
////        User user = userService.findUserById(userId);
////        Chat chat = chatService.findChatById(req.chatId());
////
////        Message message = Message.builder()
////                .chat(chat)
////                .user(user)
////                .content(req.content())
////                .timeStamp(LocalDateTime.now())
////                .readBy(new HashSet<>(Set.of(user.getId())))
////                .build();
////
////        chat.getMessages().add(message);
////
////        return messageRepository.save(message);
////    }
//
//    @Override
//    public Message sendMessage(SendMessageRequestDTO req, UUID userId)
//            throws UserException, ChatException {
//
//        User user = userService.findUserById(userId);
//        Chat chat = chatService.findChatById(req.chatId());
//
//        Message message = Message.builder()
//                .chat(chat)
//                .user(user)
//                .content(req.content())
//                .timeStamp(LocalDateTime.now())
//                .readBy(new HashSet<>(Set.of(user.getId())))
//                .build();
//
//        chat.getMessages().add(message);
//
//        Message savedMessage = messageRepository.save(message);
//
//        // 🔥 REALTIME broadcast
//        for (User u : chat.getUsers()) {
//            String destination = "/topic/" + u.getId();
//            messagingTemplate.convertAndSend(destination, MessageDTO.fromMessage(savedMessage));
//        }
//
//        return savedMessage;
//    }
//
//
//    @Override
//    public List<Message> getChatMessages(UUID chatId, User reqUser) throws UserException, ChatException {
//
//        Chat chat = chatService.findChatById(chatId);
//
//        if (!chat.getUsers().contains(reqUser)) {
//            throw new UserException("User isn't related to chat " + chatId);
//        }
//
//        return messageRepository.findByChat_Id(chat.getId());
//    }
//
//    @Override
//    public Message findMessageById(UUID messageId) throws MessageException {
//
//        Optional<Message> message = messageRepository.findById(messageId);
//
//        if (message.isPresent()) {
//            return message.get();
//        }
//
//        throw new MessageException("Message not found " + messageId);
//    }
//
//    @Override
//    public void deleteMessageById(UUID messageId, User reqUser) throws UserException, MessageException {
//
//        Message message = findMessageById(messageId);
//
//        if (message.getUser().getId().equals(reqUser.getId())) {
//            messageRepository.deleteById(messageId);
//            return;
//        }
//
//        throw new UserException("User is not related to message " + message.getId());
//    }
//
//}



package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.MessageRepository;
import com.sein_gar_har.Services.ChatService;
import com.sein_gar_har.Services.MessageService;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.dto.request.SendMessageRequestDTO;
import com.sein_gar_har.dto.response.MessageDTO;
import com.sein_gar_har.entity.Chat;
import com.sein_gar_har.entity.Message;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.ChatException;
import com.sein_gar_har.exception.MessageException;
import com.sein_gar_har.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    @Autowired
    UserService userService;

    @Autowired
    ChatService chatService;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public Message sendMessage(SendMessageRequestDTO req, UUID userId)
            throws UserException, ChatException {

        User user = userService.findUserById(userId);
        Chat chat = chatService.findChatById(req.chatId());

        Message.MessageBuilder messageBuilder = Message.builder()
                .chat(chat)
                .user(user)
                .content(req.content())
                .timeStamp(LocalDateTime.now())
                .readBy(new HashSet<>(Set.of(user.getId())));

        // Handle file upload
        if (req.file() != null && !req.file().isEmpty()) {
            try {
                String filePath = saveFile(req.file(), "files");
                String fileName = req.file().getOriginalFilename();
                String fileType = getFileType(req.file().getContentType());

                messageBuilder
                        .filePath(filePath)
                        .fileName(fileName)
                        .fileType(fileType)
                        .fileSize(req.file().getSize())
                        .mimeType(req.file().getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save file", e);
            }
        }

        // Handle voice message upload
        if (req.voiceFile() != null && !req.voiceFile().isEmpty()) {
            try {
                String voiceFilePath = saveFile(req.voiceFile(), "voice");
                messageBuilder
                        .voiceFilePath(voiceFilePath)
                        .voiceFileName(req.voiceFile().getOriginalFilename())
                        .voiceDuration(req.voiceDuration());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save voice file", e);
            }
        }

        Message message = messageBuilder.build();
        chat.getMessages().add(message);

        Message savedMessage = messageRepository.save(message);

        // Broadcast to all chat users
        for (User u : chat.getUsers()) {
            String destination = "/topic/" + u.getId();
            messagingTemplate.convertAndSend(destination, MessageDTO.fromMessage(savedMessage));
        }

        return savedMessage;
    }

    private String saveFile(MultipartFile file, String subDirectory) throws IOException {
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null && originalFileName.contains(".") ?
                originalFileName.substring(originalFileName.lastIndexOf(".")) :
                getFileExtension(file.getContentType());

        String uniqueFileName = UUID.randomUUID() + fileExtension;

        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        return String.format("%s/%s", subDirectory, uniqueFileName);
    }

    private String getFileExtension(String mimeType) {
        if (mimeType == null) return ".bin";

        switch (mimeType) {
            case "image/jpeg": return ".jpg";
            case "image/png": return ".png";
            case "image/gif": return ".gif";
            case "image/webp": return ".webp";
            case "video/mp4": return ".mp4";
            case "video/webm": return ".webm";
            case "audio/mpeg": return ".mp3";
            case "audio/wav": return ".wav";
            case "audio/webm": return ".webm";
            case "audio/ogg": return ".ogg";
            default: return ".dat";
        }
    }

    private String getFileType(String mimeType) {
        if (mimeType == null) return "document";

        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        return "document";
    }

    @Override
    public List<Message> getChatMessages(UUID chatId, User reqUser) throws UserException, ChatException {
        Chat chat = chatService.findChatById(chatId);

        if (!chat.getUsers().contains(reqUser)) {
            throw new UserException("User isn't related to chat " + chatId);
        }

        return messageRepository.findByChat_Id(chat.getId());
    }

    @Override
    public Message findMessageById(UUID messageId) throws MessageException {
        Optional<Message> message = messageRepository.findById(messageId);

        if (message.isPresent()) {
            return message.get();
        }

        throw new MessageException("Message not found " + messageId);
    }

    @Override
    public void deleteMessageById(UUID messageId, User reqUser) throws UserException, MessageException {
        Message message = findMessageById(messageId);

        if (message.getUser().getId().equals(reqUser.getId())) {
            messageRepository.deleteById(messageId);
            return;
        }

        throw new UserException("User is not related to message " + message.getId());
    }
}