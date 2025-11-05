package com.spring.Services;

import com.spring.DTO.request.SpaceRequestDTO;
import com.spring.DTO.request.SpaceUpdateRequestDTO;
import com.spring.DTO.response.SpaceResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SpaceService {
    SpaceResponseDTO createSpace(SpaceRequestDTO spaceRequestDTO);
    List<SpaceResponseDTO> getAllSpaces();
    SpaceResponseDTO getSpaceById(UUID id);
    SpaceResponseDTO updateSpace(UUID id, SpaceUpdateRequestDTO spaceUpdateRequestDTO); // Updated this method
    boolean deleteSpace(UUID id);
    List<SpaceResponseDTO> getSpacesByFloorId(Integer floorId);
    List<SpaceResponseDTO> getSpacesBySpaceTypeId(UUID spaceTypeId);
    void deleteSpaceImage(UUID spaceId, String imageUrl);
}