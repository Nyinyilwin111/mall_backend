package com.spring.Services;


import com.spring.DTO.request.SpaceTypeRequestDTO;
import com.spring.DTO.response.SpaceTypeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SpaceTypeService {
    SpaceTypeResponseDTO createSpaceType(SpaceTypeRequestDTO spaceTypeRequestDTO);
    List<SpaceTypeResponseDTO> getAllSpaceTypes();
    SpaceTypeResponseDTO getSpaceTypeById(UUID id);
    SpaceTypeResponseDTO updateSpaceType(UUID id, SpaceTypeRequestDTO spaceTypeRequestDTO);
    boolean deleteSpaceType(UUID id);
}