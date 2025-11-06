package com.spring.Services.ServiceImplements;

import com.spring.Config.S3Config;
import com.spring.Services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private S3Config s3Config;

    @Override
    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        try {
            // Generate unique file name
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String key = folderName + "/" + fileName;

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                RequestBody.fromBytes(file.getBytes()));

            // Return S3 URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                s3Config.getBucketName(), 
                s3Config.getRegion(), 
                key);

        } catch (IOException e) {
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(fileKey)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                    r -> r.getObjectRequest(getObjectRequest)
                          .signatureDuration(Duration.ofMinutes(10)) // 10 minutes expiry
            );

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String extractKeyFromUrl(String fileUrl) {
        // More robust way to extract the key from S3 URL
        String baseUrlPattern = "https://" + s3Config.getBucketName() + ".s3." + s3Config.getRegion() + ".amazonaws.com/";
        if (fileUrl.startsWith(baseUrlPattern)) {
            return fileUrl.substring(baseUrlPattern.length());
        }
        
        // Alternative pattern in case the URL format is different
        String[] parts = fileUrl.split(s3Config.getBucketName() + "\\.s3\\." + s3Config.getRegion() + "\\.amazonaws\\.com/");
        if (parts.length > 1) {
            return parts[1];
        }
        
        throw new IllegalArgumentException("Invalid S3 URL format: " + fileUrl);
    }
}