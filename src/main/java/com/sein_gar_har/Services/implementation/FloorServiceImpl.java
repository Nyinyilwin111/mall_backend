package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.FloorRepository;
import com.sein_gar_har.Services.FloorService;
import com.sein_gar_har.dto.request.FloorRequestDTO;
import com.sein_gar_har.dto.response.FloorResponseDTO;
import com.sein_gar_har.entity.Floor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FloorServiceImpl implements FloorService {

    @Autowired
    private FloorRepository floorRepository;

    private FloorResponseDTO convertToDTO(Floor floor) {
        FloorResponseDTO dto = new FloorResponseDTO();
        dto.setFloorId(floor.getFloorId());
        dto.setLevel(floor.getLevel());
        dto.setBranchBranchId(floor.getBranchBranchId());
        return dto;
    }

    private Floor convertToEntity(FloorRequestDTO dto) {
        Floor floor = new Floor();
        floor.setLevel(dto.getLevel());
        floor.setBranchBranchId(dto.getBranchBranchId());
        return floor;
    }

    @Override
    public FloorResponseDTO createFloor(FloorRequestDTO floorRequestDTO) {
        Floor floor = convertToEntity(floorRequestDTO);
        Floor savedFloor = floorRepository.save(floor);
        return convertToDTO(savedFloor);
    }

    @Override
    public List<FloorResponseDTO> getAllFloors() {
        return floorRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FloorResponseDTO getFloorById(Integer id) {
        Optional<Floor> floor = floorRepository.findById(id);
        return floor.map(this::convertToDTO).orElse(null);
    }

    @Override
    public FloorResponseDTO updateFloor(Integer id, FloorRequestDTO floorRequestDTO) {
        Optional<Floor> optionalFloor = floorRepository.findById(id);
        if (optionalFloor.isPresent()) {
            Floor floor = optionalFloor.get();
            floor.setLevel(floorRequestDTO.getLevel());
            floor.setBranchBranchId(floorRequestDTO.getBranchBranchId());
            Floor updatedFloor = floorRepository.save(floor);
            return convertToDTO(updatedFloor);
        }
        return null;
    }

    @Override
    public boolean deleteFloor(Integer id) {
        if (floorRepository.existsById(id)) {
            floorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<FloorResponseDTO> getFloorsByBranchId(Integer branchId) {
        return floorRepository.findByBranchBranchId(branchId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}