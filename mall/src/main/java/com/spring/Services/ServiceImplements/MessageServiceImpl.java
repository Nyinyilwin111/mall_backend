package com.spring.Services.ServiceImplements;

import com.spring.DTO.request.SendMessageRequestDTO;
import com.spring.Entity.Chat;
import com.spring.Entity.Message;
import com.spring.Entity.User;
import com.spring.Exceptions.ChatException;
import com.spring.Exceptions.MessageException;
import com.spring.Exceptions.UserException;
import com.spring.RepositoryMain.MessageRepository;
import com.spring.Services.ChatService;
import com.spring.Services.MessageService;
import com.spring.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    UserService userService;

    @Autowired
    @Lazy
    ChatService chatService;

    @Autowired
    MessageRepository messageRepository;

    @Override
    public Message sendMessage(SendMessageRequestDTO req, UUID userId) throws ChatException, UserException {
        User user = userService.findUserById(userId);
        Chat chat = chatService.findChatById(req.chatId());

        Message message = Message.builder()
                .chat(chat)
                .user(user)
                .content(req.content())
                .timeStamp(LocalDateTime.now())
                .readBy(new HashSet<>(Set.of(user.getId())))
                .build();

        chat.getMessages().add(message);

        return messageRepository.save(message);
    }

    @Override
    public List<Message> getChatMessages(UUID chatId, User reqUser) throws UserException, ChatException {

        Chat chat = chatService.findChatById(chatId);

        if (!chat.getUsers().contains(reqUser)) {
            throw new UserException("User isn't related to chat " + chatId);
        }

        return messageRepository.findMessagesByChatAndUser(chat.getId(),reqUser.getId());
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

    @Override
    public Message sendMessageWithFile(UUID chatId, String content, MultipartFile file, UUID userId)
            throws UserException, ChatException {

        System.out.println("this is in service implement");
        User user = userService.findUserById(userId);
        Chat chat = chatService.findChatById(chatId);

        String filePath = null;

        if (file != null && !file.isEmpty()) {
            System.out.println("Before saveFile");
            filePath = saveFile(file);
            System.out.println("After saveFile: " + filePath);
            System.out.println("Uploaded file size: " + getReadableFileSize(file.getSize()));
            System.out.println("File path: " + filePath);
        }

        // Build message
        Message message = Message.builder()
                .chat(chat)
                .user(user)
                .timeStamp(LocalDateTime.now())
                .readBy(new HashSet<>(Set.of(user.getId())))
                .content(content != null && !content.isBlank() ? content : null)
                .filePath(filePath)
                .build();

        System.out.println("this is in service implement--------third------");

        // Persist message
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteAllMessagesByChatId(UUID chatId) throws ChatException {
        System.out.println("reach in message service implement");
        Chat chat = chatService.findChatById(chatId);

        // Delete files if present
        for (Message message : chat.getMessages()) {
            if (message.getFilePath() != null && !message.getFilePath().isBlank()) {
                deleteFileFromSystem(message.getFilePath());
            }
        }

        // Clear messages list -> orphanRemoval deletes from DB
        chat.getMessages().clear();
        System.out.println("all messages cleared from chat");
    }


    private void deleteFileFromSystem(String filePath) {
        try {
            Path absolutePath = Paths.get(System.getProperty("user.dir")).resolve(filePath);
            File fileToDelete = absolutePath.toFile();
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    System.err.println("Failed to delete file: " + filePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }

    // Save the file physically inside the project
    private String saveFile(MultipartFile file) throws ChatException {
        try {
            // Absolute path to project folder
            String uploadsDir = System.getProperty("user.dir") + "/uploads";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadsDir);
            java.nio.file.Files.createDirectories(uploadPath);

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = uploadPath.resolve(filename);

            System.out.println("Saving file to: " + filePath.toAbsolutePath());
            file.transferTo(filePath.toFile());

            return "uploads/" + filename; // store relative path in DB
        } catch (Exception e) {
            e.printStackTrace(); // print full stacktrace
            throw new ChatException("Failed to save file: " + e.getMessage());
        }
    }

    // Place the readable size method here
    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
