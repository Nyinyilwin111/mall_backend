package com.sein_gar_har.dto.response;

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

    // Full constructor
    public BranchResponseDTO(Long id, String name, String address, String phoneNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Optional constructor for only id and name
    public BranchResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // No-args constructor (required by some frameworks)
    public BranchResponseDTO() {}
}
