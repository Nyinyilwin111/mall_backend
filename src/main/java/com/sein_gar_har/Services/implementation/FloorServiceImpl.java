package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.FloorRepository;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.Services.FloorService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.FloorRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.dto.response.FloorResponseDTO;
import com.sein_gar_har.entity.Floor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private BranchService branchService;


    private final AuditLogService auditLogService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private HttpServletRequest request;

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
    @Transactional
    public FloorResponseDTO createFloor(FloorRequestDTO floorRequestDTO) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== CREATE FLOOR - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Floor floor = convertToEntity(floorRequestDTO);
            Floor savedFloor = floorRepository.save(floor);

            // Audit log for floor creation
            Map<String, Object> newData = new HashMap<>();
            newData.put("floorId", savedFloor.getFloorId());
            newData.put("level", savedFloor.getLevel());
            newData.put("branchBranchId", savedFloor.getBranchBranchId());

            auditLogService.logCreate("Floor", savedFloor.getFloorId().toString(), newData);

            return convertToDTO(savedFloor);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
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
    @Transactional
    public FloorResponseDTO updateFloor(Integer id, FloorRequestDTO floorRequestDTO) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== UPDATE FLOOR - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<Floor> optionalFloor = floorRepository.findById(id);
            if (optionalFloor.isPresent()) {
                Floor floor = optionalFloor.get();

                // Store old data for audit log
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("floorId", floor.getFloorId());
                oldData.put("level", floor.getLevel());
                oldData.put("branchBranchId", floor.getBranchBranchId());

                floor.setLevel(floorRequestDTO.getLevel());
                floor.setBranchBranchId(floorRequestDTO.getBranchBranchId());

                Floor updatedFloor = floorRepository.save(floor);

                // Store new data for audit log
                Map<String, Object> newData = new HashMap<>();
                newData.put("floorId", updatedFloor.getFloorId());
                newData.put("level", updatedFloor.getLevel());
                newData.put("branchBranchId", updatedFloor.getBranchBranchId());

                auditLogService.logUpdate("Floor", id.toString(), oldData, newData);

                return convertToDTO(updatedFloor);
            }
            return null;
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean deleteFloor(Integer id) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== DELETE FLOOR - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<Floor> floorOptional = floorRepository.findById(id);
            if (floorOptional.isPresent()) {
                Floor floor = floorOptional.get();

                // Store data for audit log before deletion
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("floorId", floor.getFloorId());
                oldData.put("level", floor.getLevel());
                oldData.put("branchBranchId", floor.getBranchBranchId());

                floorRepository.deleteById(id);

                auditLogService.logDelete("Floor", id.toString(), oldData);

                return true;
            }
            return false;
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    public List<FloorResponseDTO> getFloorsByBranchId(Integer branchId) {
        return floorRepository.findByBranchBranchId(branchId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Enhanced JWT token extraction with multiple fallback strategies
     */
    private String extractUserFromJwtTokenWithFallback() {
        // Strategy 1: Try to extract from JWT token
        String userFromJwt = extractUserFromJwtToken();
        if (!"System".equals(userFromJwt)) {
            return userFromJwt;
        }

        // Strategy 2: Try to get from request header directly (for testing)
        String userFromHeader = extractUserFromCustomHeader();
        if (!"System".equals(userFromHeader)) {
            return userFromHeader;
        }

        // Strategy 3: Return a default test user for now
        return "Test User";
    }

    /**
     * Extract user information from JWT token
     */
    private String extractUserFromJwtToken() {
        try {
            String token = extractTokenFromRequest();
            if (token != null && !token.trim().isEmpty()) {
                System.out.println("=== JWT TOKEN EXTRACTION ===");
                System.out.println("Token length: " + token.length());

                // Validate token format
                if (!token.contains(".") || token.split("\\.").length != 3) {
                    System.out.println("Invalid JWT token format");
                    return "System";
                }

                try {
                    // Use TokenProvider to get claims from token
                    var claims = tokenProvider.getClaimsFromToken(token);
                    String email = claims.get(JwtConstants.EMAIL, String.class);

                    System.out.println("Email from token: " + email);

                    if (email != null && !email.trim().isEmpty()) {
                        return email;
                    } else {
                        System.out.println("No email found in JWT token claims");
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing JWT token: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("No JWT token found in request");
            }
        } catch (Exception e) {
            System.err.println("Error extracting user from JWT token: " + e.getMessage());
            e.printStackTrace();
        }

        return "System";
    }

    /**
     * Extract JWT token from request header
     */
    private String extractTokenFromRequest() {
        try {
            // Check multiple possible header names
            String authHeader = request.getHeader(JwtConstants.TOKEN_HEADER);
            if (authHeader == null) {
                authHeader = request.getHeader("Authorization");
            }

            System.out.println("Authorization Header: " + (authHeader != null ? authHeader : "Null"));

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                System.out.println("Extracted token length: " + token.length());
                return token;
            } else {
                System.out.println("No Bearer token found in header. Header value: " + authHeader);
            }
        } catch (Exception e) {
            System.err.println("Error extracting token from request: " + e.getMessage());
        }
        return null;
    }

    /**
     * Fallback method: Extract user from custom header (for testing)
     */
    private String extractUserFromCustomHeader() {
        try {
            // Check if there's a custom header with user info (for testing)
            String testUser = request.getHeader("X-Test-User");
            if (testUser != null && !testUser.trim().isEmpty()) {
                System.out.println("Found test user from header: " + testUser);
                return testUser;
            }
        } catch (Exception e) {
            System.err.println("Error extracting user from custom header: " + e.getMessage());
        }
        return "System";
    }
    private String getBranchName(Long branchId) {
        try {
            BranchResponseDTO branch = branchService.getBranchById(branchId);
            return branch != null ? branch.getName() : "Branch " + branchId;
        } catch (Exception e) {
            return "Branch " + branchId;
        }
    }
}