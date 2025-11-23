package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.FloorRepository;
import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.RepositoryMain.SpaceTypeRepository;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.Services.LocalStorageService;
import com.sein_gar_har.Services.SpaceService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.enums.SpaceStatus;
import com.sein_gar_har.dto.request.SpaceRequestDTO;
import com.sein_gar_har.dto.request.SpaceUpdateRequestDTO;
import com.sein_gar_har.dto.response.FloorResponseDTO;
import com.sein_gar_har.dto.response.SpaceResponseDTO;
import com.sein_gar_har.dto.response.SpaceTypeResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.Floor;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.entity.SpaceType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceServiceImpl implements SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceTypeRepository spaceTypeRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private LocalStorageService localStorageService;

    @Autowired
    private BranchRepository branchRepository;

    private final AuditLogService auditLogService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private HttpServletRequest request;

    private SpaceResponseDTO convertToDTO(Space space) {
        SpaceResponseDTO dto = new SpaceResponseDTO();
        dto.setSpaceId(space.getSpaceId());
        dto.setSpaceCode(space.getSpaceCode());
        dto.setLocation(space.getLocation());
        dto.setSizeSqft(space.getSizeSqft());
        dto.setPrice(space.getPrice());
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
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== CREATE SPACE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            // CHECK IF SPACE CODE ALREADY EXISTS
            if (spaceRepository.existsBySpaceCode(spaceRequestDTO.getSpaceCode())) {
                throw new RuntimeException("Space code '" + spaceRequestDTO.getSpaceCode() + "' already exists");
            }

            Optional<SpaceType> spaceType = spaceTypeRepository.findById(spaceRequestDTO.getSpaceTypeId());
            Optional<Floor> floor = floorRepository.findById(spaceRequestDTO.getFloorId());

            if (spaceType.isEmpty() || floor.isEmpty()) {
                throw new RuntimeException("SpaceType or Floor not found");
            }

            Space space = new Space();
            space.setSpaceCode(spaceRequestDTO.getSpaceCode());
            space.setSpaceType(spaceType.get());
            space.setLocation(spaceRequestDTO.getLocation());
            space.setSizeSqft(spaceRequestDTO.getSizeSqft());
            space.setPrice(spaceRequestDTO.getPrice());
            space.setAmenities(spaceRequestDTO.getAmenities());
            space.setFloor(floor.get());

            if (spaceRequestDTO.getStatus() != null) {
                space.setStatus(Space.SpaceStatus.valueOf(spaceRequestDTO.getStatus().name()));
            } else {
                space.setStatus(Space.SpaceStatus.VACANT);
            }

            if (spaceRequestDTO.getImages() != null && !spaceRequestDTO.getImages().isEmpty()) {
                List<String> imageUrls = uploadMultipleFiles(spaceRequestDTO.getImages());
                if (imageUrls.size() > 4) {
                    imageUrls = imageUrls.subList(0, 4);
                }
                space.setImages(imageUrls);
            }

            Space savedSpace = spaceRepository.save(space);

            // Audit log for space creation
            Map<String, Object> newData = new HashMap<>();
            newData.put("spaceId", savedSpace.getSpaceId());
            newData.put("spaceCode", savedSpace.getSpaceCode());
            newData.put("location", savedSpace.getLocation());
            newData.put("sizeSqft", savedSpace.getSizeSqft());
            newData.put("price", savedSpace.getPrice());
            newData.put("amenities", savedSpace.getAmenities());
            newData.put("status", savedSpace.getStatus());
            newData.put("spaceType", savedSpace.getSpaceType() != null ? savedSpace.getSpaceType().getTypeName() : null);
            newData.put("floor", savedSpace.getFloor() != null ? savedSpace.getFloor().getLevel() : null);
            newData.put("imagesCount", savedSpace.getImages() != null ? savedSpace.getImages().size() : 0);

            auditLogService.logCreate("Space", savedSpace.getSpaceId().toString(), newData);

            return convertToDTO(savedSpace);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
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

    public SpaceResponseDTO getSpaceByCode(String spaceCode) {
        Optional<Space> space = spaceRepository.findBySpaceCode(spaceCode);
        return space.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional
    public SpaceResponseDTO updateSpace(UUID id, SpaceUpdateRequestDTO spaceUpdateRequestDTO) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== UPDATE SPACE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<Space> optionalSpace = spaceRepository.findById(id);
            if (optionalSpace.isPresent()) {
                Space space = optionalSpace.get();

                // Store old data for audit log
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("spaceId", space.getSpaceId());
                oldData.put("spaceCode", space.getSpaceCode());
                oldData.put("location", space.getLocation());
                oldData.put("sizeSqft", space.getSizeSqft());
                oldData.put("price", space.getPrice());
                oldData.put("amenities", space.getAmenities());
                oldData.put("status", space.getStatus());
                oldData.put("spaceType", space.getSpaceType() != null ? space.getSpaceType().getTypeName() : null);
                oldData.put("floor", space.getFloor() != null ? space.getFloor().getLevel() : null);
                oldData.put("imagesCount", space.getImages() != null ? space.getImages().size() : 0);

                // CHECK IF SPACE CODE IS BEING UPDATED AND IF IT'S UNIQUE
                if (spaceUpdateRequestDTO.getSpaceCode() != null &&
                        !spaceUpdateRequestDTO.getSpaceCode().equals(space.getSpaceCode())) {

                    if (spaceRepository.existsBySpaceCodeAndSpaceIdNot(spaceUpdateRequestDTO.getSpaceCode(), id)) {
                        throw new RuntimeException("Space code '" + spaceUpdateRequestDTO.getSpaceCode() + "' already exists");
                    }
                    space.setSpaceCode(spaceUpdateRequestDTO.getSpaceCode());
                }

                space.setLocation(spaceUpdateRequestDTO.getLocation());
                space.setSizeSqft(spaceUpdateRequestDTO.getSizeSqft());
                space.setPrice(spaceUpdateRequestDTO.getPrice());
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
                    List<String> newImageUrls = uploadMultipleFiles(spaceUpdateRequestDTO.getNewImages());
                    updatedImages.addAll(newImageUrls);
                }

                if (updatedImages.size() > 4) {
                    updatedImages = updatedImages.subList(0, 4);
                }

                space.setImages(updatedImages);

                Space updatedSpace = spaceRepository.save(space);

                // Store new data for audit log
                Map<String, Object> newData = new HashMap<>();
                newData.put("spaceId", updatedSpace.getSpaceId());
                newData.put("spaceCode", updatedSpace.getSpaceCode());
                newData.put("location", updatedSpace.getLocation());
                newData.put("sizeSqft", updatedSpace.getSizeSqft());
                newData.put("price", updatedSpace.getPrice());
                newData.put("amenities", updatedSpace.getAmenities());
                newData.put("status", updatedSpace.getStatus());
                newData.put("spaceType", updatedSpace.getSpaceType() != null ? updatedSpace.getSpaceType().getTypeName() : null);
                newData.put("floor", updatedSpace.getFloor() != null ? updatedSpace.getFloor().getLevel() : null);
                newData.put("imagesCount", updatedSpace.getImages() != null ? updatedSpace.getImages().size() : 0);

                auditLogService.logUpdate("Space", id.toString(), oldData, newData);

                return convertToDTO(updatedSpace);
            }
            return null;
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean deleteSpace(UUID id) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== DELETE SPACE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<Space> spaceOptional = spaceRepository.findById(id);
            if (spaceOptional.isPresent()) {
                Space space = spaceOptional.get();

                // Store data for audit log before deletion
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("spaceId", space.getSpaceId());
                oldData.put("spaceCode", space.getSpaceCode());
                oldData.put("location", space.getLocation());
                oldData.put("sizeSqft", space.getSizeSqft());
                oldData.put("price", space.getPrice());
                oldData.put("amenities", space.getAmenities());
                oldData.put("status", space.getStatus());
                oldData.put("spaceType", space.getSpaceType() != null ? space.getSpaceType().getTypeName() : null);
                oldData.put("floor", space.getFloor() != null ? space.getFloor().getLevel() : null);
                oldData.put("imagesCount", space.getImages() != null ? space.getImages().size() : 0);

                if (space.getImages() != null && !space.getImages().isEmpty()) {
                    deleteMultipleFiles(space.getImages());
                }

                spaceRepository.deleteById(id);

                auditLogService.logDelete("Space", id.toString(), oldData);

                return true;
            }
            return false;
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public void deleteSpaceImage(UUID spaceId, String imageUrl) {
        // Get current user for audit log
        String currentUser = extractUserFromJwtTokenWithFallback();
        System.out.println("=== DELETE SPACE IMAGE - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Optional<Space> spaceOptional = spaceRepository.findById(spaceId);
            if (spaceOptional.isPresent()) {
                Space space = spaceOptional.get();

                // Store old data for audit log
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("spaceId", space.getSpaceId());
                oldData.put("spaceCode", space.getSpaceCode());
                oldData.put("images", space.getImages());

                if (space.getImages().remove(imageUrl)) {
                    localStorageService.deleteFile(imageUrl);
                    Space updatedSpace = spaceRepository.save(space);

                    // Store new data for audit log
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("spaceId", updatedSpace.getSpaceId());
                    newData.put("spaceCode", updatedSpace.getSpaceCode());
                    newData.put("images", updatedSpace.getImages());
                    newData.put("deletedImage", imageUrl);

                    auditLogService.logUpdate("Space", spaceId.toString(), oldData, newData);
                }
            } else {
                throw new RuntimeException("Space not found with id: " + spaceId);
            }
        } finally {
            AuditLogService.clearBranchOperationUser();
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

    public boolean spaceCodeExists(String spaceCode) {
        return spaceRepository.existsBySpaceCode(spaceCode);
    }

    @Override
    public Map<String, Object> getSpaceWithBranchData(UUID spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        Map<String, Object> result = new HashMap<>();

        // Space data
        result.put("spaceId", space.getSpaceId());
        result.put("spaceCode", space.getSpaceCode());
        result.put("location", space.getLocation());
        result.put("price", space.getPrice());
        result.put("sizeSqft", space.getSizeSqft());
        result.put("status", space.getStatus());

        // Branch data
        if (space.getFloor() != null && space.getFloor().getBranchBranchId() != null) {
            Optional<Branch> branch = branchRepository.findById(Long.valueOf(space.getFloor().getBranchBranchId()));
            if (branch.isPresent()) {
                result.put("branchId", branch.get().getId());
                result.put("branchName", branch.get().getName());
                result.put("branchAddress", branch.get().getAddress());
                result.put("branchPhone", branch.get().getPhoneNumber());
            } else {
                result.put("branchName", "Unknown Branch");
            }
        } else {
            result.put("branchName", "No Branch Assigned");
        }

        return result;
    }

    // Helper methods for local storage
    private List<String> uploadMultipleFiles(List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileUrl = localStorageService.saveFile(file);
                if (fileUrl != null) {
                    fileUrls.add(fileUrl);
                }
            }
        }
        return fileUrls;
    }

    private void deleteMultipleFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            localStorageService.deleteFile(fileUrl);
        }
    }

    /**
     * Enhanced JWT token extraction with proper user extraction
     */
    private String extractUserFromJwtTokenWithFallback() {
        try {
            // Strategy 1: Try to extract from JWT token (primary method)
            String userFromJwt = extractUserFromJwtToken();
            if (userFromJwt != null && !userFromJwt.equals("System") && !userFromJwt.isEmpty()) {
                System.out.println("=== USING JWT USER: " + userFromJwt + " ===");
                return userFromJwt;
            }

            // Strategy 2: Try to get from request header directly (for testing/fallback)
            String userFromHeader = extractUserFromCustomHeader();
            if (userFromHeader != null && !userFromHeader.equals("System") && !userFromHeader.isEmpty()) {
                System.out.println("=== USING HEADER USER: " + userFromHeader + " ===");
                return userFromHeader;
            }

            // Strategy 3: Last resort - use a more descriptive default
            System.out.println("=== USING DEFAULT USER (no JWT/user found) ===");
            return "Unknown User";

        } catch (Exception e) {
            System.err.println("Error extracting user: " + e.getMessage());
            return "System Error";
        }
    }

    /**
     * Extract user information from JWT token - IMPROVED VERSION
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
                    return null;
                }

                try {
                    // Use TokenProvider to get claims from token
                    var claims = tokenProvider.getClaimsFromToken(token);

                    // Try multiple possible claim names for user identification
                    String email = claims.get(JwtConstants.EMAIL, String.class);
                    String username = claims.get("username", String.class);
                    String sub = claims.get("sub", String.class);

                    System.out.println("JWT Claims found:");
                    System.out.println("Email: " + email);
                    System.out.println("Username: " + username);
                    System.out.println("Subject: " + sub);

                    // Priority: email -> username -> subject
                    if (email != null && !email.trim().isEmpty()) {
                        return email;
                    } else if (username != null && !username.trim().isEmpty()) {
                        return username;
                    } else if (sub != null && !sub.trim().isEmpty()) {
                        return sub;
                    } else {
                        System.out.println("No user identifier found in JWT token claims");
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing JWT token: " + e.getMessage());
                    return null;
                }
            } else {
                System.out.println("No JWT token found in request");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error extracting user from JWT token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract JWT token from request header - IMPROVED VERSION
     */
    private String extractTokenFromRequest() {
        try {
            // Check multiple possible header names
            String authHeader = request.getHeader(JwtConstants.TOKEN_HEADER);
            if (authHeader == null) {
                authHeader = request.getHeader("Authorization");
            }

            // Additional fallback headers
            if (authHeader == null) {
                authHeader = request.getHeader("X-Authorization");
            }

            System.out.println("Authorization Header: " + (authHeader != null ? authHeader : "Null"));

            if (authHeader != null) {
                // Handle different token formats
                if (authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7).trim();
                    System.out.println("Extracted Bearer token length: " + token.length());
                    return token;
                } else if (authHeader.startsWith("Basic ")) {
                    System.out.println("Basic auth found, not JWT");
                    return null;
                } else {
                    // Assume it's a raw token
                    String token = authHeader.trim();
                    System.out.println("Extracted raw token length: " + token.length());
                    return token;
                }
            } else {
                System.out.println("No authorization header found");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error extracting token from request: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fallback method: Extract user from custom header (for testing) - IMPROVED
     */
    private String extractUserFromCustomHeader() {
        try {
            // Check if there's a custom header with user info (for testing)
            String testUser = request.getHeader("X-Test-User");
            if (testUser != null && !testUser.trim().isEmpty()) {
                System.out.println("Found test user from header: " + testUser);
                return testUser;
            }

            // Additional fallback headers
            String usernameHeader = request.getHeader("X-Username");
            if (usernameHeader != null && !usernameHeader.trim().isEmpty()) {
                System.out.println("Found username from header: " + usernameHeader);
                return usernameHeader;
            }

            String emailHeader = request.getHeader("X-User-Email");
            if (emailHeader != null && !emailHeader.trim().isEmpty()) {
                System.out.println("Found email from header: " + emailHeader);
                return emailHeader;
            }
        } catch (Exception e) {
            System.err.println("Error extracting user from custom header: " + e.getMessage());
        }
        return null;
    }
}