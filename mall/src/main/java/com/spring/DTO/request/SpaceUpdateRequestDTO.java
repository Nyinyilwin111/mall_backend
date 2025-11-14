package com.spring.DTO.request;

import com.spring.DTO.enums.SpaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;
@Data
public class SpaceUpdateRequestDTO {

    private String spaceCode; // ADD THIS FIELD - Can be updated

    private UUID spaceTypeId;

    private Integer floorId;

    @Positive(message = "Price must be positive")
    private Double price; // ADD THIS FIELD

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Size is required")
    @Positive(message = "Size must be positive")
    private Double sizeSqft;

    private String amenities;

    private SpaceStatus status;

    private List<MultipartFile> newImages;

    private List<String> existingImages;



}