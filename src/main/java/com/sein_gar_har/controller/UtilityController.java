package com.sein_gar_har.controller;

import com.sein_gar_har.Services.UtilityService;
import com.sein_gar_har.dto.request.UtilityRequestDTO;
import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
import com.sein_gar_har.dto.response.UtilityResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilities")
@CrossOrigin(origins = "*")
public class UtilityController {

    @Autowired
    private UtilityService utilityService;

    // ✅ CREATE UTILITY - POST: http://localhost:8081/api/utilities/create
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUtility(@RequestBody UtilityRequestDTO utilityRequestDTO) {
        try {
            System.out.println("Received utility creation request:");
            System.out.println("Space ID: " + utilityRequestDTO.getSpaceId());
            System.out.println("Utility Type: " + utilityRequestDTO.getUtilityType());
            System.out.println("Previous Reading: " + utilityRequestDTO.getPreviousReading());
            System.out.println("Current Reading: " + utilityRequestDTO.getCurrentReading());

            UtilityResponseDTO createdUtility = utilityService.createUtility(utilityRequestDTO);
            return ResponseEntity.ok(createdUtility);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating utility: " + e.getMessage());
        }
    }

    // ✅ GET ALL UTILITIES - GET: http://localhost:8081/api/utilities/getAll
    @GetMapping("/getAll")
    public ResponseEntity<List<UtilityResponseDTO>> getAllUtilities() {
        List<UtilityResponseDTO> utilities = utilityService.getAllUtilities();
        return ResponseEntity.ok(utilities);
    }

    // ✅ GET UTILITY BY ID - GET: http://localhost:8081/api/utilities/get/{id}
    @GetMapping("/get/{id}")
    public ResponseEntity<UtilityResponseDTO> getUtilityById(@PathVariable Long id) {
        UtilityResponseDTO utility = utilityService.getUtilityById(id);
        if (utility != null) {
            return ResponseEntity.ok(utility);
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ GET UTILITIES BY SPACE ID - GET: http://localhost:8081/api/utilities/getspace/{spaceId}
    @GetMapping("/getspace/{spaceId}")
    public ResponseEntity<List<UtilityResponseDTO>> getUtilitiesBySpaceId(@PathVariable UUID spaceId) {
        List<UtilityResponseDTO> utilities = utilityService.getUtilitiesBySpaceId(spaceId);
        return ResponseEntity.ok(utilities);
    }

    // ✅ UPDATE UTILITY - PUT: http://localhost:8081/api/utilities/update/{id}
    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUtility(@PathVariable Long id, @RequestBody UtilityUpdateRequestDTO utilityUpdateRequestDTO) {
        try {
            System.out.println("Received utility update request for ID: " + id);

            UtilityResponseDTO updatedUtility = utilityService.updateUtility(id, utilityUpdateRequestDTO);
            if (updatedUtility != null) {
                return ResponseEntity.ok(updatedUtility);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating utility: " + e.getMessage());
        }
    }

    // ✅ DELETE UTILITY - DELETE: http://localhost:8081/api/utilities/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtility(@PathVariable Long id) {
        if (utilityService.deleteUtility(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Keep payment endpoints but they return empty/default values
    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<?> markUtilityAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok().body("Payment functionality disabled");
    }

    @GetMapping("/getspace/{spaceId}/pending")
    public ResponseEntity<List<UtilityResponseDTO>> getPendingUtilitiesBySpaceId(@PathVariable UUID spaceId) {
        List<UtilityResponseDTO> utilities = utilityService.getPendingUtilitiesBySpaceId(spaceId);
        return ResponseEntity.ok(utilities);
    }

    @GetMapping("/getspace/{spaceId}/total-pending")
    public ResponseEntity<BigDecimal> getTotalPendingAmountBySpaceId(@PathVariable UUID spaceId) {
        BigDecimal total = utilityService.getTotalPendingAmountBySpaceId(spaceId);
        return ResponseEntity.ok(total);
    }
}