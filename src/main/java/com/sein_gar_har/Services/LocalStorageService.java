package com.sein_gar_har.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${server.port:8081}")
    private String serverPort;

    public String saveFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return null;
            }

            // ✅ make path absolute to project root
            Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir)
                    .toAbsolutePath()
                    .normalize();

            // folder မရှိရင် create လုပ်
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());

            // ✅ Return absolute URL for frontend access
            return "http://localhost:" + serverPort + "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally", e);
        }
    }

    public boolean deleteFile(String fileUrl) {
        try {
            // Extract filename from both relative and absolute URLs
            String fileName;
            if (fileUrl.contains("/uploads/")) {
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/uploads/") + 9);
            } else {
                fileName = fileUrl.replace("/uploads/", "");
            }

            Path filePath = Paths.get(System.getProperty("user.dir"), uploadDir, fileName)
                    .toAbsolutePath()
                    .normalize();
            File file = filePath.toFile();
            return file.exists() && file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}