package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.SpaceRequestDTO;
import com.sein_gar_har.dto.request.SpaceUpdateRequestDTO;
import com.sein_gar_har.dto.response.SpaceResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SpaceService {

    SpaceResponseDTO createSpace(SpaceRequestDTO spaceRequestDTO);

    List<SpaceResponseDTO> getAllSpaces();

    SpaceResponseDTO getSpaceById(UUID id);

    SpaceResponseDTO getSpaceByCode(String spaceCode); // ADD THIS METHOD

    SpaceResponseDTO updateSpace(UUID id, SpaceUpdateRequestDTO spaceUpdateRequestDTO);

    boolean deleteSpace(UUID id);

    List<SpaceResponseDTO> getSpacesByFloorId(Integer floorId);

    List<SpaceResponseDTO> getSpacesBySpaceTypeId(UUID spaceTypeId);

    void deleteSpaceImage(UUID spaceId, String imageUrl);

    boolean spaceCodeExists(String spaceCode); // ADD THIS METHOD

    Map<String, Object> getSpaceWithBranchData(UUID spaceId);
}