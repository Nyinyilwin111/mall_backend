package com.spring.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) {
        try {
            String key = generateFileName(file.getOriginalFilename());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));

            return generateFileUrl(key);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        } catch (S3Exception e) {
            throw new RuntimeException("S3 error occurred: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        List<String> uploadedUrls = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = uploadFile(file);
                    uploadedUrls.add(fileUrl);
                }
            }
        }

        return uploadedUrls;
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void deleteMultipleFiles(List<String> fileUrls) {
        if (fileUrls != null) {
            for (String fileUrl : fileUrls) {
                deleteFile(fileUrl);
            }
        }
    }

    private String generateFileName(String originalFileName) {
        return "spaces/" + UUID.randomUUID() + "_" + originalFileName;
    }

    private String generateFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, key);
    }

    private String extractKeyFromUrl(String fileUrl) {
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        return fileUrl.replace(baseUrl, "");
    }
}