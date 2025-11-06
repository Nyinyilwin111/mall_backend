//package com.spring.Services;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.spring.S3ConfigProperties;
//
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//public class S3Service {
//
//    @Autowired
//    private S3ConfigProperties s3Config;
//
//    private S3Client s3Client() {
//        return S3Client.builder()
//                .region(Region.of(s3Config.getRegion()))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())
//                ))
//                .build();
//    }
//
//    public String uploadFile(MultipartFile file) throws IOException {
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("File is empty");
//        }
//
//        String fileName = generateFileName(file.getOriginalFilename());
//        
//        try (S3Client s3Client = s3Client()) {
//            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                    .bucket(s3Config.getBucketName())
//                    .key(fileName)
//                    .contentType(file.getContentType())
//                    .build();
//
//            s3Client.putObject(putObjectRequest, 
//                    RequestBody.fromBytes(file.getBytes()));
//
//            return String.format("https://%s.s3.%s.amazonaws.com/%s", 
//                    s3Config.getBucketName(), s3Config.getRegion(), fileName);
//        }
//    }
//
//    public void deleteFile(String fileUrl) {
//        try {
//            String fileName = extractFileNameFromUrl(fileUrl);
//            
//            try (S3Client s3Client = s3Client()) {
//                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
//                        .bucket(s3Config.getBucketName())
//                        .key(fileName)
//                        .build();
//
//                s3Client.deleteObject(deleteObjectRequest);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Error deleting file from S3: " + e.getMessage(), e);
//        }
//    }
//
//    private String generateFileName(String originalFileName) {
//        return UUID.randomUUID().toString() + "_" + originalFileName;
//    }
//
//    private String extractFileNameFromUrl(String fileUrl) {
//        // Extract filename from URL: https://bucket.s3.region.amazonaws.com/filename
//        String[] parts = fileUrl.split("/");
//        return parts[parts.length - 1];
//    }
//}
package com.spring.Services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface S3Service {
    String uploadFile(MultipartFile file, String folderName) throws IOException;
    void deleteFile(String fileUrl);
    String generatePresignedUrl(String fileKey);
}