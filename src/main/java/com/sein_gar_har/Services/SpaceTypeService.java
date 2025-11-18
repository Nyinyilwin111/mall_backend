package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.SpaceTypeRequestDTO;
import com.sein_gar_har.dto.response.SpaceTypeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SpaceTypeService {

    SpaceTypeResponseDTO createSpaceType(SpaceTypeRequestDTO spaceTypeRequestDTO);

    List<SpaceTypeResponseDTO> getAllSpaceTypes();

    SpaceTypeResponseDTO getSpaceTypeById(UUID id);

    SpaceTypeResponseDTO updateSpaceType(UUID id, SpaceTypeRequestDTO spaceTypeRequestDTO);

    boolean deleteSpaceType(UUID id);
}