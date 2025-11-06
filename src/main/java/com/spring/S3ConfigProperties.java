package com.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws.s3")
public class S3ConfigProperties {
    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;

    // Getters and setters
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
}