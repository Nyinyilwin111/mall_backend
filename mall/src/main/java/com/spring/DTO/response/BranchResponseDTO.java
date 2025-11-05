package com.spring.DTO.response;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BranchResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}