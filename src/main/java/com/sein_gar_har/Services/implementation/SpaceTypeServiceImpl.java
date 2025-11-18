package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.SpaceTypeRepository;
import com.sein_gar_har.Services.SpaceTypeService;
import com.sein_gar_har.dto.request.SpaceTypeRequestDTO;
import com.sein_gar_har.dto.response.SpaceTypeResponseDTO;
import com.sein_gar_har.entity.SpaceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpaceTypeServiceImpl implements SpaceTypeService {

    @Autowired
    private SpaceTypeRepository spaceTypeRepository;

    private SpaceTypeResponseDTO convertToDTO(SpaceType spaceType) {
        SpaceTypeResponseDTO dto = new SpaceTypeResponseDTO();
        dto.setSpaceTypeId(spaceType.getSpaceTypeId());
        dto.setTypeName(spaceType.getTypeName());
        dto.setDescription(spaceType.getDescription());
        dto.setCreatedAt(spaceType.getCreatedAt());
        return dto;
    }

    private SpaceType convertToEntity(SpaceTypeRequestDTO dto) {
        SpaceType spaceType = new SpaceType();
        spaceType.setTypeName(dto.getTypeName());
        spaceType.setDescription(dto.getDescription());
        return spaceType;
    }

    @Override
    public SpaceTypeResponseDTO createSpaceType(SpaceTypeRequestDTO spaceTypeRequestDTO) {
        SpaceType spaceType = convertToEntity(spaceTypeRequestDTO);
        SpaceType savedSpaceType = spaceTypeRepository.save(spaceType);
        return convertToDTO(savedSpaceType);
    }

    @Override
    public List<SpaceTypeResponseDTO> getAllSpaceTypes() {
        return spaceTypeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceTypeResponseDTO getSpaceTypeById(UUID id) {
        Optional<SpaceType> spaceType = spaceTypeRepository.findById(id);
        return spaceType.map(this::convertToDTO).orElse(null);
    }

    @Override
    public SpaceTypeResponseDTO updateSpaceType(UUID id, SpaceTypeRequestDTO spaceTypeRequestDTO) {
        Optional<SpaceType> optionalSpaceType = spaceTypeRepository.findById(id);
        if (optionalSpaceType.isPresent()) {
            SpaceType spaceType = optionalSpaceType.get();
            spaceType.setTypeName(spaceTypeRequestDTO.getTypeName());
            spaceType.setDescription(spaceTypeRequestDTO.getDescription());
            SpaceType updatedSpaceType = spaceTypeRepository.save(spaceType);
            return convertToDTO(updatedSpaceType);
        }
        return null;
    }

    @Override
    public boolean deleteSpaceType(UUID id) {
        if (spaceTypeRepository.existsById(id)) {
            spaceTypeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}