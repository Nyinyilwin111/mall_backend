package com.sein_gar_har.Services;


import com.sein_gar_har.dto.request.FloorRequestDTO;
import com.sein_gar_har.dto.response.FloorResponseDTO;

import java.util.List;

public interface FloorService {
    FloorResponseDTO createFloor(FloorRequestDTO floorRequestDTO);
    List<FloorResponseDTO> getAllFloors();
    FloorResponseDTO getFloorById(Integer id);
    FloorResponseDTO updateFloor(Integer id, FloorRequestDTO floorRequestDTO);
    boolean deleteFloor(Integer id);
    List<FloorResponseDTO> getFloorsByBranchId(Integer branchId);
}