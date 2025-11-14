package com.spring.DTO.response;

import com.spring.DTO.enums.SpaceStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SpaceResponseDTO {
    private UUID spaceId;
    private String spaceCode; // ADD THIS FIELD
    private SpaceTypeResponseDTO spaceType;
    private String location;
    private Double sizeSqft;
    private Double price; // ADD THIS FIELD
    private String amenities;
    private List<String> images;
    private SpaceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private FloorResponseDTO floor;
}