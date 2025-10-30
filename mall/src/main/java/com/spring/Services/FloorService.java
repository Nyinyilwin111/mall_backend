package com.spring.Services;


import com.spring.DTO.request.FloorRequestDTO;
import com.spring.DTO.response.FloorResponseDTO;

import java.util.List;

public interface FloorService {
    FloorResponseDTO createFloor(FloorRequestDTO floorRequestDTO);
    List<FloorResponseDTO> getAllFloors();
    FloorResponseDTO getFloorById(Integer id);
    FloorResponseDTO updateFloor(Integer id, FloorRequestDTO floorRequestDTO);
    boolean deleteFloor(Integer id);
    List<FloorResponseDTO> getFloorsByBranchId(Integer branchId);
}