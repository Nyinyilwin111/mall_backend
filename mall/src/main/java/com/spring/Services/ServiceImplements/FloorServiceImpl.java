package com.spring.Services.ServiceImplements;

import com.spring.DTO.request.FloorRequestDTO;
import com.spring.DTO.response.FloorResponseDTO;
import com.spring.Entity.Floor;
import com.spring.Repository.FloorRepository;
import com.spring.Services.FloorService;
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