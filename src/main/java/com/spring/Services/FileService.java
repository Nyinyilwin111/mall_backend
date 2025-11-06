//package com.example.imageTest.Service;
//
//import com.example.imageTest.Model.ImageFile;
//import com.example.imageTest.Repository.ImageFileRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.S3Exception;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//public class FileService {
//
//    private final S3Client s3Client;
//    private final ImageFileRepository imageFileRepository;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucketName;
//
//    public FileService(S3Client s3Client, ImageFileRepository imageFileRepository) {
//        this.s3Client = s3Client;
//        this.imageFileRepository = imageFileRepository;
//    }
//
//    public String uploadFile(MultipartFile file) throws IOException {
//        String originalFileName = file.getOriginalFilename();
//        String fileName = UUID.randomUUID() + "_" + originalFileName;
//        String fileType = file.getContentType();
//        Long fileSize = file.getSize();
//
//        try {
//            // Upload to S3
//            s3Client.putObject(
//                PutObjectRequest.builder()
//                        .bucket(bucketName)
//                        .key(fileName)
//                        .contentType(fileType)
//                        .build(),
//                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
//            );
//            
//            // Generate URL
//            String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
//            
//            // Save to Database (optional - only if you want to track all files)
//            ImageFile imageFile = new ImageFile(originalFileName, fileUrl, fileType, fileSize);
//            imageFileRepository.save(imageFile);
//            
//            return fileUrl;
//        } catch (S3Exception e) {
//            throw new RuntimeException("S3 Upload failed: " + e.awsErrorDetails().errorMessage());
//        }
//    }
//
//    // Additional method to handle lease contract files specifically
//    public String uploadLeaseContract(MultipartFile file, Long leaseId) throws IOException {
//        String originalFileName = file.getOriginalFilename();
//        String fileName = "leases/" + leaseId + "/contract_" + UUID.randomUUID() + "_" + originalFileName;
//        String fileType = file.getContentType();
//        Long fileSize = file.getSize();
//
//        try {
//            // Upload to S3 with organized folder structure
//            s3Client.putObject(
//                PutObjectRequest.builder()
//                        .bucket(bucketName)
//                        .key(fileName)
//                        .contentType(fileType)
//                        .build(),
//                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
//            );
//            
//            // Generate URL
//            String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
//            
//            // Save to Database
//            ImageFile imageFile = new ImageFile(originalFileName, fileUrl, fileType, fileSize);
//            imageFileRepository.save(imageFile);
//            
//            return fileUrl;
//        } catch (S3Exception e) {
//            throw new RuntimeException("S3 Upload failed: " + e.awsErrorDetails().errorMessage());
//        }
//    }
//}