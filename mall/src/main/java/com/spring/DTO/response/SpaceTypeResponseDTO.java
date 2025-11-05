package com.spring.DTO.response;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SpaceTypeResponseDTO {
    private UUID spaceTypeId;
    private String typeName;
    private String description;
    private LocalDateTime createdAt;
}