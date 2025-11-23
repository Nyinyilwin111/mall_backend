package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.SpaceTypeRepository;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.Services.SpaceTypeService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.SpaceTypeRequestDTO;
import com.sein_gar_har.dto.response.SpaceTypeResponseDTO;
import com.sein_gar_har.entity.SpaceType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceTypeServiceImpl implements SpaceTypeService {

    @Autowired
    private SpaceTypeRepository spaceTypeRepository;

    private final AuditLogService auditLogService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private HttpServletRequest request;

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
    @Transactional
    public SpaceTypeResponseDTO createSpaceType(SpaceTypeRequestDTO spaceTypeRequestDTO) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== CREATE SPACE TYPE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            SpaceType spaceType = convertToEntity(spaceTypeRequestDTO);
            SpaceType savedSpaceType = spaceTypeRepository.save(spaceType);

            // Audit log for space type creation
            Map<String, Object> newData = new HashMap<>();
            newData.put("spaceTypeId", savedSpaceType.getSpaceTypeId());
            newData.put("typeName", savedSpaceType.getTypeName());
            newData.put("description", savedSpaceType.getDescription());

            auditLogService.logCreate("SpaceType", savedSpaceType.getSpaceTypeId().toString(), newData);

            return convertToDTO(savedSpaceType);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
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
    @Transactional
    public SpaceTypeResponseDTO updateSpaceType(UUID id, SpaceTypeRequestDTO spaceTypeRequestDTO) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== UPDATE SPACE TYPE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<SpaceType> optionalSpaceType = spaceTypeRepository.findById(id);
            if (optionalSpaceType.isPresent()) {
                SpaceType spaceType = optionalSpaceType.get();

                // Store old data for audit log
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("spaceTypeId", spaceType.getSpaceTypeId());
                oldData.put("typeName", spaceType.getTypeName());
                oldData.put("description", spaceType.getDescription());

                spaceType.setTypeName(spaceTypeRequestDTO.getTypeName());
                spaceType.setDescription(spaceTypeRequestDTO.getDescription());

                SpaceType updatedSpaceType = spaceTypeRepository.save(spaceType);

                // Store new data for audit log
                Map<String, Object> newData = new HashMap<>();
                newData.put("spaceTypeId", updatedSpaceType.getSpaceTypeId());
                newData.put("typeName", updatedSpaceType.getTypeName());
                newData.put("description", updatedSpaceType.getDescription());

                auditLogService.logUpdate("SpaceType", id.toString(), oldData, newData);

                return convertToDTO(updatedSpaceType);
            }
            return null;
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean deleteSpaceType(UUID id) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== DELETE SPACE TYPE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<SpaceType> spaceTypeOptional = spaceTypeRepository.findById(id);
            if (spaceTypeOptional.isPresent()) {
                SpaceType spaceType = spaceTypeOptional.get();

                // Store data for audit log before deletion
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("spaceTypeId", spaceType.getSpaceTypeId());
                oldData.put("typeName", spaceType.getTypeName());
                oldData.put("description", spaceType.getDescription());

                spaceTypeRepository.deleteById(id);

                auditLogService.logDelete("SpaceType", id.toString(), oldData);

                return true;
            }
            return false;
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
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
}