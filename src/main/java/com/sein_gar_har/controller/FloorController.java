package com.sein_gar_har.controller;

import com.sein_gar_har.Services.FloorService;
import com.sein_gar_har.dto.request.FloorRequestDTO;
import com.sein_gar_har.dto.response.FloorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/floors")
public class FloorController {

    @Autowired
    private FloorService floorService;

    @PostMapping
    public ResponseEntity<FloorResponseDTO> createFloor(@Valid @RequestBody FloorRequestDTO floorRequestDTO) {
        FloorResponseDTO createdFloor = floorService.createFloor(floorRequestDTO);
        return ResponseEntity.ok(createdFloor);
    }

    @GetMapping
    public ResponseEntity<List<FloorResponseDTO>> getAllFloors() {
        List<FloorResponseDTO> floors = floorService.getAllFloors();
        return ResponseEntity.ok(floors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FloorResponseDTO> getFloorById(@PathVariable Integer id) {
        FloorResponseDTO floor = floorService.getFloorById(id);
        if (floor != null) {
            return ResponseEntity.ok(floor);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<FloorResponseDTO> updateFloor(@PathVariable Integer id, @Valid @RequestBody FloorRequestDTO floorRequestDTO) {
        FloorResponseDTO updatedFloor = floorService.updateFloor(id, floorRequestDTO);
        if (updatedFloor != null) {
            return ResponseEntity.ok(updatedFloor);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFloor(@PathVariable Integer id) {
        if (floorService.deleteFloor(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<FloorResponseDTO>> getFloorsByBranchId(@PathVariable Integer branchId) {
        List<FloorResponseDTO> floors = floorService.getFloorsByBranchId(branchId);
        return ResponseEntity.ok(floors);
    }


}