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
public class SpaceRequestDTO {
    private UUID spaceTypeId;
    private String location;
    private Double sizeSqft;
    private String amenities;
    private List<MultipartFile> images;
    private Integer floorId;
    private SpaceStatus status;

    // Add validation annotations
    @NotNull(message = "Space type ID is required")
    public UUID getSpaceTypeId() {
        return spaceTypeId;
    }

    @NotBlank(message = "Location is required")
    public String getLocation() {
        return location;
    }

    @NotNull(message = "Size is required")
    @Positive(message = "Size must be positive")
    public Double getSizeSqft() {
        return sizeSqft;
    }

    @NotNull(message = "Floor ID is required")
    public Integer getFloorId() {
        return floorId;
    }
}