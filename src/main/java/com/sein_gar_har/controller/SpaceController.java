package com.sein_gar_har.controller;

import com.sein_gar_har.Services.SpaceService;
import com.sein_gar_har.dto.request.SpaceRequestDTO;
import com.sein_gar_har.dto.request.SpaceUpdateRequestDTO;
import com.sein_gar_har.dto.response.SpaceResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/spaces")
@CrossOrigin(origins = "*")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSpace(@ModelAttribute SpaceRequestDTO spaceRequestDTO) {
        try {
            System.out.println("Received space creation request:");
            System.out.println("SpaceTypeId: " + spaceRequestDTO.getSpaceTypeId());
            System.out.println("Location: " + spaceRequestDTO.getLocation());
            System.out.println("SizeSqft: " + spaceRequestDTO.getSizeSqft());
            System.out.println("FloorId: " + spaceRequestDTO.getFloorId());
            System.out.println("Status: " + spaceRequestDTO.getStatus());
            System.out.println("Amenities: " + spaceRequestDTO.getAmenities());
            System.out.println("Images count: " + (spaceRequestDTO.getImages() != null ? spaceRequestDTO.getImages().size() : 0));

            SpaceResponseDTO createdSpace = spaceService.createSpace(spaceRequestDTO);
            return ResponseEntity.ok(createdSpace);
        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging
            return ResponseEntity.badRequest().body("Error creating space: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SpaceResponseDTO>> getAllSpaces() {
        List<SpaceResponseDTO> spaces = spaceService.getAllSpaces();
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceResponseDTO> getSpaceById(@PathVariable UUID id) {
        SpaceResponseDTO space = spaceService.getSpaceById(id);
        if (space != null) {
            return ResponseEntity.ok(space);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateSpace(@PathVariable UUID id, @ModelAttribute SpaceUpdateRequestDTO spaceUpdateRequestDTO) {
        try {
            System.out.println("Received space update request for ID: " + id);
            System.out.println("SpaceTypeId: " + spaceUpdateRequestDTO.getSpaceTypeId());
            System.out.println("Location: " + spaceUpdateRequestDTO.getLocation());
            System.out.println("Existing images: " + spaceUpdateRequestDTO.getExistingImages());

            SpaceResponseDTO updatedSpace = spaceService.updateSpace(id, spaceUpdateRequestDTO);
            if (updatedSpace != null) {
                return ResponseEntity.ok(updatedSpace);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating space: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable UUID id) {
        if (spaceService.deleteSpace(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<Void> deleteSpaceImage(@PathVariable UUID id, @RequestParam String imageUrl) {
        try {
            spaceService.deleteSpaceImage(id, imageUrl);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/floor/{floorId}")
    public ResponseEntity<List<SpaceResponseDTO>> getSpacesByFloorId(@PathVariable Integer floorId) {
        List<SpaceResponseDTO> spaces = spaceService.getSpacesByFloorId(floorId);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/space-type/{spaceTypeId}")
    public ResponseEntity<List<SpaceResponseDTO>> getSpacesBySpaceTypeId(@PathVariable UUID spaceTypeId) {
        List<SpaceResponseDTO> spaces = spaceService.getSpacesBySpaceTypeId(spaceTypeId);
        return ResponseEntity.ok(spaces);
    }

    // Add these methods to your SpaceController

    @GetMapping("/code/{spaceCode}")
    public ResponseEntity<SpaceResponseDTO> getSpaceByCode(@PathVariable String spaceCode) {
        SpaceResponseDTO space = spaceService.getSpaceByCode(spaceCode);
        if (space != null) {
            return new ResponseEntity<>(space, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/check-code/{spaceCode}")
    public ResponseEntity<Map<String, Boolean>> checkSpaceCodeExists(@PathVariable String spaceCode) {
        boolean exists = spaceService.spaceCodeExists(spaceCode);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}