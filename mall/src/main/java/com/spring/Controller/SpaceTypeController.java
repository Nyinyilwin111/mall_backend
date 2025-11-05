package com.spring.Controller;

import com.spring.DTO.request.SpaceTypeRequestDTO;
import com.spring.DTO.response.SpaceTypeResponseDTO;
import com.spring.Services.SpaceTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/space-types")
public class SpaceTypeController {

    @Autowired
    private SpaceTypeService spaceTypeService;

    @PostMapping
    public ResponseEntity<SpaceTypeResponseDTO> createSpaceType(@Valid @RequestBody SpaceTypeRequestDTO spaceTypeRequestDTO) {
        SpaceTypeResponseDTO createdSpaceType = spaceTypeService.createSpaceType(spaceTypeRequestDTO);
        return ResponseEntity.ok(createdSpaceType);
    }

    @GetMapping
    public ResponseEntity<List<SpaceTypeResponseDTO>> getAllSpaceTypes() {
        List<SpaceTypeResponseDTO> spaceTypes = spaceTypeService.getAllSpaceTypes();
        return ResponseEntity.ok(spaceTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceTypeResponseDTO> getSpaceTypeById(@PathVariable UUID id) {
        SpaceTypeResponseDTO spaceType = spaceTypeService.getSpaceTypeById(id);
        if (spaceType != null) {
            return ResponseEntity.ok(spaceType);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceTypeResponseDTO> updateSpaceType(@PathVariable UUID id, @Valid @RequestBody SpaceTypeRequestDTO spaceTypeRequestDTO) {
        SpaceTypeResponseDTO updatedSpaceType = spaceTypeService.updateSpaceType(id, spaceTypeRequestDTO);
        if (updatedSpaceType != null) {
            return ResponseEntity.ok(updatedSpaceType);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpaceType(@PathVariable UUID id) {
        if (spaceTypeService.deleteSpaceType(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}