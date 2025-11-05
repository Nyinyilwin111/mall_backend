package com.spring.Services.ServiceImplements;

import com.spring.DTO.enums.SpaceStatus;
import com.spring.DTO.request.SpaceRequestDTO;
import com.spring.DTO.request.SpaceUpdateRequestDTO;
import com.spring.DTO.response.FloorResponseDTO;
import com.spring.DTO.response.SpaceResponseDTO;
import com.spring.DTO.response.SpaceTypeResponseDTO;
import com.spring.Entity.Space;
import com.spring.Entity.SpaceType;
import com.spring.Entity.Floor;
import com.spring.Repository.SpaceRepository;
import com.spring.Repository.SpaceTypeRepository;
import com.spring.Repository.FloorRepository;
import com.spring.Services.S3Service;
import com.spring.Services.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpaceServiceImpl implements SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceTypeRepository spaceTypeRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private S3Service s3Service;

    private SpaceResponseDTO convertToDTO(Space space) {
        SpaceResponseDTO dto = new SpaceResponseDTO();
        dto.setSpaceId(space.getSpaceId());
        dto.setLocation(space.getLocation());
        dto.setSizeSqft(space.getSizeSqft());
        dto.setAmenities(space.getAmenities());
        dto.setCreatedAt(space.getCreatedAt());
        dto.setUpdatedAt(space.getUpdatedAt());
        dto.setImages(space.getImages());

        if (space.getStatus() != null) {
            dto.setStatus(SpaceStatus.valueOf(space.getStatus().name()));
        } else {
            dto.setStatus(SpaceStatus.VACANT);
        }

        if (space.getSpaceType() != null) {
            SpaceTypeResponseDTO spaceTypeDTO = new SpaceTypeResponseDTO();
            spaceTypeDTO.setSpaceTypeId(space.getSpaceType().getSpaceTypeId());
            spaceTypeDTO.setTypeName(space.getSpaceType().getTypeName());
            spaceTypeDTO.setDescription(space.getSpaceType().getDescription());
            spaceTypeDTO.setCreatedAt(space.getSpaceType().getCreatedAt());
            dto.setSpaceType(spaceTypeDTO);
        }

        if (space.getFloor() != null) {
            FloorResponseDTO floorDTO = new FloorResponseDTO();
            floorDTO.setFloorId(space.getFloor().getFloorId());
            floorDTO.setLevel(space.getFloor().getLevel());
            floorDTO.setBranchBranchId(space.getFloor().getBranchBranchId());
            dto.setFloor(floorDTO);
        }

        return dto;
    }

    @Override
    @Transactional
    public SpaceResponseDTO createSpace(SpaceRequestDTO spaceRequestDTO) {
        Optional<SpaceType> spaceType = spaceTypeRepository.findById(spaceRequestDTO.getSpaceTypeId());
        Optional<Floor> floor = floorRepository.findById(spaceRequestDTO.getFloorId());

        if (spaceType.isEmpty() || floor.isEmpty()) {
            throw new RuntimeException("SpaceType or Floor not found");
        }

        Space space = new Space();
        space.setSpaceType(spaceType.get());
        space.setLocation(spaceRequestDTO.getLocation());
        space.setSizeSqft(spaceRequestDTO.getSizeSqft());
        space.setAmenities(spaceRequestDTO.getAmenities());
        space.setFloor(floor.get());

        if (spaceRequestDTO.getStatus() != null) {
            space.setStatus(Space.SpaceStatus.valueOf(spaceRequestDTO.getStatus().name()));
        } else {
            space.setStatus(Space.SpaceStatus.VACANT);
        }

        if (spaceRequestDTO.getImages() != null && !spaceRequestDTO.getImages().isEmpty()) {
            List<String> imageUrls = s3Service.uploadMultipleFiles(spaceRequestDTO.getImages());
            if (imageUrls.size() > 4) {
                imageUrls = imageUrls.subList(0, 4);
            }
            space.setImages(imageUrls);
        }

        Space savedSpace = spaceRepository.save(space);
        return convertToDTO(savedSpace);
    }

    @Override
    public List<SpaceResponseDTO> getAllSpaces() {
        return spaceRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceResponseDTO getSpaceById(UUID id) {
        Optional<Space> space = spaceRepository.findById(id);
        return space.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional
    public SpaceResponseDTO updateSpace(UUID id, SpaceUpdateRequestDTO spaceUpdateRequestDTO) {
        Optional<Space> optionalSpace = spaceRepository.findById(id);
        if (optionalSpace.isPresent()) {
            Space space = optionalSpace.get();

            space.setLocation(spaceUpdateRequestDTO.getLocation());
            space.setSizeSqft(spaceUpdateRequestDTO.getSizeSqft());
            space.setAmenities(spaceUpdateRequestDTO.getAmenities());

            if (spaceUpdateRequestDTO.getStatus() != null) {
                space.setStatus(Space.SpaceStatus.valueOf(spaceUpdateRequestDTO.getStatus().name()));
            }

            if (spaceUpdateRequestDTO.getSpaceTypeId() != null) {
                Optional<SpaceType> spaceType = spaceTypeRepository.findById(spaceUpdateRequestDTO.getSpaceTypeId());
                spaceType.ifPresent(space::setSpaceType);
            }

            if (spaceUpdateRequestDTO.getFloorId() != null) {
                Optional<Floor> floor = floorRepository.findById(spaceUpdateRequestDTO.getFloorId());
                floor.ifPresent(space::setFloor);
            }

            List<String> updatedImages = new ArrayList<>();

            if (spaceUpdateRequestDTO.getExistingImages() != null) {
                updatedImages.addAll(spaceUpdateRequestDTO.getExistingImages());
            }

            if (spaceUpdateRequestDTO.getNewImages() != null && !spaceUpdateRequestDTO.getNewImages().isEmpty()) {
                List<String> newImageUrls = s3Service.uploadMultipleFiles(spaceUpdateRequestDTO.getNewImages());
                updatedImages.addAll(newImageUrls);
            }

            if (updatedImages.size() > 4) {
                updatedImages = updatedImages.subList(0, 4);
            }

            space.setImages(updatedImages);

            Space updatedSpace = spaceRepository.save(space);
            return convertToDTO(updatedSpace);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean deleteSpace(UUID id) {
        Optional<Space> spaceOptional = spaceRepository.findById(id);
        if (spaceOptional.isPresent()) {
            Space space = spaceOptional.get();

            if (space.getImages() != null && !space.getImages().isEmpty()) {
                s3Service.deleteMultipleFiles(space.getImages());
            }

            spaceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteSpaceImage(UUID spaceId, String imageUrl) {
        Optional<Space> spaceOptional = spaceRepository.findById(spaceId);
        if (spaceOptional.isPresent()) {
            Space space = spaceOptional.get();

            if (space.getImages().remove(imageUrl)) {
                s3Service.deleteFile(imageUrl);
                spaceRepository.save(space);
            }
        } else {
            throw new RuntimeException("Space not found with id: " + spaceId);
        }
    }

    @Override
    public List<SpaceResponseDTO> getSpacesByFloorId(Integer floorId) {
        return spaceRepository.findByFloorFloorId(floorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceResponseDTO> getSpacesBySpaceTypeId(UUID spaceTypeId) {
        return spaceRepository.findBySpaceTypeSpaceTypeId(spaceTypeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}