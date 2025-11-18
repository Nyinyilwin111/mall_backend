package com.sein_gar_har.dto.response;


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