package com.sein_gar_har.controller;

import com.sein_gar_har.Services.LeaseService;
import com.sein_gar_har.Services.SpaceService;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.dto.request.LeaseRequest;
import com.sein_gar_har.dto.response.LeaseResponse;
import com.sein_gar_har.dto.response.SpaceResponseDTO;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import com.sein_gar_har.Services.AuditLogService;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/leases")
public class LeaseController {

    @Autowired
    LeaseService leaseService;

    @Autowired
    UserService userService;

    @Autowired
    SpaceService spaceService;

    @Autowired
    AuditLogService auditLogService;

    // CREATE
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLease(@ModelAttribute LeaseRequest leaseRequest) {
        try {
            LeaseResponse createdLease = leaseService.createLease(leaseRequest);
            return ResponseEntity.ok(createdLease);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating lease: " + e.getMessage());
        }
    }

    // READ ALL
    @GetMapping("/getAll")
    public ResponseEntity<List<LeaseResponse>> getAllLeases() {
        return ResponseEntity.ok(leaseService.getAllLeases());
    }

    // READ ONE
    @GetMapping("/get/{id}")
    public ResponseEntity<LeaseResponse> getLeaseById(@PathVariable Long id) {
        LeaseResponse lease = leaseService.getLeaseById(id);
        return (lease != null) ? ResponseEntity.ok(lease) : ResponseEntity.notFound().build();
    }

    // UPDATE
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLease(@PathVariable Long id, @ModelAttribute LeaseRequest leaseRequest) {
        try {
            LeaseResponse updatedLease = leaseService.updateLease(id, leaseRequest);
            return (updatedLease != null) ? ResponseEntity.ok(updatedLease) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating lease: " + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
        return leaseService.deleteLease(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // 🔥 FIXED — GET MY LEASE DETAILS (JWT instead of Principal)
    @GetMapping("/my-leases/{id}/details")
    public ResponseEntity<LeaseResponse> getMyLeaseDetails(
            @PathVariable Long id,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt) {

        try {
            User user = userService.findUserByProfile(jwt);
            LeaseResponse lease = leaseService.getLeaseWithDetails(id);

            if (!lease.getTenantId().equals(user.getId().toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(lease);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 🔥 FIXED — GET LEASES OF CURRENT JWT USER
    @GetMapping("/my-leases")
    public ResponseEntity<List<LeaseResponse>> getMyLeases(
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt) {

        try {
            User user = userService.findUserByProfile(jwt);
            List<LeaseResponse> leases =
                    leaseService.getLeasesByTenantId(user.getId().toString())
                            .stream()
                            .map(LeaseResponse::new)
                            .collect(Collectors.toList());

            return ResponseEntity.ok(leases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 🔥 FIXED — GET MY SPACES USING JWT TOKEN
    @GetMapping("/my-spaces")
    public ResponseEntity<List<SpaceResponseDTO>> getMyLeasedSpaces(
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt) {

        try {
            User user = userService.findUserByProfile(jwt);

            List<Lease> leases = leaseService.getLeasesByTenantId(user.getId().toString());

            List<SpaceResponseDTO> spaces = leases.stream()
                    .map(lease -> spaceService.getSpaceById(lease.getSpace().getSpaceId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 🔥 ADD THIS — GET MY SPACES BY BRANCH
    @GetMapping("/my-spaces/branch/{branchId}")
    public ResponseEntity<List<SpaceResponseDTO>> getMyLeasedSpacesByBranch(
            @PathVariable Long branchId,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt) {

        try {
            User user = userService.findUserByProfile(jwt);
            List<Lease> leases = leaseService.getLeasesByTenantIdAndBranchId(
                    user.getId().toString(),
                    branchId
            );

            List<SpaceResponseDTO> spaces = leases.stream()
                    .map(lease -> spaceService.getSpaceById(lease.getSpace().getSpaceId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // DEBUG (optional)
    @GetMapping("/debug/leases")
    public ResponseEntity<List<Map<String, Object>>> debugLeases(
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt) {

        try {
            User user = userService.findUserByProfile(jwt);
            List<Lease> userLeases = leaseService.getLeasesByTenantId(user.getId().toString());

            List<Map<String, Object>> result = new ArrayList<>();
            for (Lease lease : userLeases) {
                Map<String, Object> item = new HashMap<>();
                item.put("leaseId", lease.getLeaseId());
                item.put("tenantId", lease.getTenant().getId());
                item.put("space", lease.getSpace().getSpaceId());
                result.add(item);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get leases for current tenant in specific branch
    // In LeaseController.java - Fix the getMyLeasesByBranch method
    // Simplified controller method using repository directly
    @GetMapping("/my-leases/branch/{branchId}")
    public ResponseEntity<List<LeaseResponse>> getMyLeasesByBranch(
            @PathVariable Long branchId,
            @RequestHeader(JwtConstants.TOKEN_HEADER) String jwt) {

        try {
            // Get current user from JWT
            User user = userService.findUserByProfile(jwt);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Get leases by tenant ID and branch ID
            List<Lease> leases = leaseService.getLeasesByTenantIdAndBranchId(
                    user.getId().toString(),
                    branchId
            );

            // Convert to LeaseResponse DTOs
            List<LeaseResponse> leaseResponses = leases.stream()
                    .map(LeaseResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(leaseResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
