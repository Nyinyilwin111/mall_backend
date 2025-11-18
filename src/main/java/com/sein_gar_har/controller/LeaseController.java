////package com.spring.Controller;
////
////import com.spring.DTO.request.LeaseRequest;
////import com.spring.DTO.response.LeaseResponse;
////import com.spring.Services.LeaseService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.MediaType;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////import org.springframework.web.multipart.MultipartException;
////
////import java.util.List;
////
////@RestController
////@CrossOrigin(origins = "*")
////@RequestMapping("/api/leases")
////public class LeaseController {
////
////    @Autowired
////    private LeaseService leaseService;
////
////    // ✅ CREATE (Frontend → /leases/create)
////    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
////    public ResponseEntity<?> createLease(@ModelAttribute LeaseRequest leaseRequest) {
////        try {
////            System.out.println("Received lease creation request:");
////            System.out.println("SpaceId: " + leaseRequest.getSpaceId());
////            System.out.println("StartDate: " + leaseRequest.getStartDate());
////            System.out.println("EndDate: " + leaseRequest.getEndDate());
////
////            LeaseResponse createdLease = leaseService.createLease(leaseRequest);
////            return ResponseEntity.ok(createdLease);
////        } catch (MultipartException e) {
////            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
////        } catch (Exception e) {
////            e.printStackTrace();
////            return ResponseEntity.badRequest().body("Error creating lease: " + e.getMessage());
////        }
////    }
////
////    // ✅ READ ALL (Frontend → /leases/getAll)
////    @GetMapping("/getAll")
////    public ResponseEntity<List<LeaseResponse>> getAllLeases() {
////        List<LeaseResponse> leases = leaseService.getAllLeases();
////        return ResponseEntity.ok(leases);
////    }
////
////    // ✅ READ ONE (Frontend → /leases/get/{id})
////    @GetMapping("/get/{id}")
////    public ResponseEntity<LeaseResponse> getLeaseById(@PathVariable Long id) {
////        LeaseResponse lease = leaseService.getLeaseById(id);
////        if (lease != null) {
////            return ResponseEntity.ok(lease);
////        }
////        return ResponseEntity.notFound().build();
////    }
////
////    // ✅ UPDATE (Frontend → /leases/update/{id})
////    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
////    public ResponseEntity<?> updateLease(@PathVariable Long id, @ModelAttribute LeaseRequest leaseRequest) {
////        try {
////            System.out.println("Received lease update request for ID: " + id);
////
////            LeaseResponse updatedLease = leaseService.updateLease(id, leaseRequest);
////            if (updatedLease != null) {
////                return ResponseEntity.ok(updatedLease);
////            }
////            return ResponseEntity.notFound().build();
////        } catch (MultipartException e) {
////            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
////        } catch (Exception e) {
////            e.printStackTrace();
////            return ResponseEntity.badRequest().body("Error updating lease: " + e.getMessage());
////        }
////    }
////
////    // ✅ DELETE (Frontend → /leases/delete/{id})
////    @DeleteMapping("/delete/{id}")
////    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
////        if (leaseService.deleteLease(id)) {
////            return ResponseEntity.ok().build();
////        }
////        return ResponseEntity.notFound().build();
////    }
////}
////package com.spring.Controller;
////
////import com.spring.DTO.request.LeaseRequest;
////import com.spring.DTO.response.LeaseResponse;
////import com.spring.Services.LeaseService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.MediaType;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////import org.springframework.web.multipart.MultipartException;
////
////import java.util.List;
////
////@RestController
////@CrossOrigin(origins = "*")
////@RequestMapping("/api/leases")
////public class LeaseController {
////
////    @Autowired
////    LeaseService leaseService;
////
////    // ✅ CREATE (Frontend → /leases/create)
////    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
////    public ResponseEntity<?> createLease(@ModelAttribute LeaseRequest leaseRequest) {
////        try {
////            System.out.println("Received lease creation request:");
////            System.out.println("SpaceId: " + leaseRequest.getSpaceId());
////            System.out.println("StartDate: " + leaseRequest.getStartDate());
////            System.out.println("EndDate: " + leaseRequest.getEndDate());
////            System.out.println("CompanyName: " + leaseRequest.getCompanyName());
////            System.out.println("TenantType: " + leaseRequest.getTenantType());
////            System.out.println("TenantTrade: " + leaseRequest.getTenantTrade());
////            System.out.println("NRC: " + leaseRequest.getNrc());
////            System.out.println("Address: " + leaseRequest.getAddress());
////            System.out.println("Heir: " + leaseRequest.getHeir());
////
////            LeaseResponse createdLease = leaseService.createLease(leaseRequest);
////            return ResponseEntity.ok(createdLease);
////        } catch (MultipartException e) {
////            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
////        } catch (Exception e) {
////            e.printStackTrace();
////            return ResponseEntity.badRequest().body("Error creating lease: " + e.getMessage());
////        }
////    }
////
////    // ✅ READ ALL (Frontend → /leases/getAll)
////    @GetMapping("/getAll")
////    public ResponseEntity<List<LeaseResponse>> getAllLeases() {
////        List<LeaseResponse> leases = leaseService.getAllLeases();
////        return ResponseEntity.ok(leases);
////    }
////
////    // ✅ READ ONE (Frontend → /leases/get/{id})
////    @GetMapping("/get/{id}")
////    public ResponseEntity<LeaseResponse> getLeaseById(@PathVariable Long id) {
////        LeaseResponse lease = leaseService.getLeaseById(id);
////        if (lease != null) {
////            return ResponseEntity.ok(lease);
////        }
////        return ResponseEntity.notFound().build();
////    }
////
////    // ✅ UPDATE (Frontend → /leases/update/{id})
////    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
////    public ResponseEntity<?> updateLease(@PathVariable Long id, @ModelAttribute LeaseRequest leaseRequest) {
////        try {
////            System.out.println("Received lease update request for ID: " + id);
////            System.out.println("CompanyName: " + leaseRequest.getCompanyName());
////            System.out.println("TenantType: " + leaseRequest.getTenantType());
////            System.out.println("TenantTrade: " + leaseRequest.getTenantTrade());
////            System.out.println("NRC: " + leaseRequest.getNrc());
////            System.out.println("Address: " + leaseRequest.getAddress());
////            System.out.println("Heir: " + leaseRequest.getHeir());
////
////            LeaseResponse updatedLease = leaseService.updateLease(id, leaseRequest);
////            if (updatedLease != null) {
////                return ResponseEntity.ok(updatedLease);
////            }
////            return ResponseEntity.notFound().build();
////        } catch (MultipartException e) {
////            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
////        } catch (Exception e) {
////            e.printStackTrace();
////            return ResponseEntity.badRequest().body("Error updating lease: " + e.getMessage());
////        }
////    }
////
////    // ✅ DELETE (Frontend → /leases/delete/{id})
////    @DeleteMapping("/delete/{id}")
////    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
////        if (leaseService.deleteLease(id)) {
////            return ResponseEntity.ok().build();
////        }
////        return ResponseEntity.notFound().build();
////    }
////}
//
//
//package com.sein_gar_har.controller;
//
//import com.sein_gar_har.Services.LeaseService;
//import com.sein_gar_har.Services.SpaceService;
//import com.sein_gar_har.Services.UserService;
//import com.sein_gar_har.dto.request.LeaseRequest;
//import com.sein_gar_har.dto.response.LeaseResponse;
//import com.sein_gar_har.dto.response.SpaceResponseDTO;
//import com.sein_gar_har.entity.Lease;
//import com.sein_gar_har.entity.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartException;
//
//import java.security.Principal;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@CrossOrigin(origins = "*")
//@RequestMapping("/api/leases")
//public class LeaseController {
//
//    @Autowired
//    LeaseService leaseService;
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    SpaceService spaceService;
//
//    // CREATE (Frontend → /leases/create)
//    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> createLease(@ModelAttribute LeaseRequest leaseRequest) {
//        try {
//            System.out.println("Received lease creation request:");
//            System.out.println("SpaceId: " + leaseRequest.getSpaceId());
//            System.out.println("StartDate: " + leaseRequest.getStartDate());
//            System.out.println("EndDate: " + leaseRequest.getEndDate());
//            System.out.println("CompanyName: " + leaseRequest.getCompanyName());
//            System.out.println("TenantType: " + leaseRequest.getTenantType());
//            System.out.println("TenantTrade: " + leaseRequest.getTenantTrade());
//            System.out.println("NRC: " + leaseRequest.getNrc());
//            System.out.println("Address: " + leaseRequest.getAddress());
//            System.out.println("Heir: " + leaseRequest.getHeir());
//
//            LeaseResponse createdLease = leaseService.createLease(leaseRequest);
//            return ResponseEntity.ok(createdLease);
//        } catch (MultipartException e) {
//            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().body("Error creating lease: " + e.getMessage());
//        }
//    }
//
//    // READ ALL (Frontend → /leases/getAll)
//    @GetMapping("/getAll")
//    public ResponseEntity<List<LeaseResponse>> getAllLeases() {
//        List<LeaseResponse> leases = leaseService.getAllLeases();
//        return ResponseEntity.ok(leases);
//    }
//
//    // READ ONE (Frontend → /leases/get/{id})
//    @GetMapping("/get/{id}")
//    public ResponseEntity<LeaseResponse> getLeaseById(@PathVariable Long id) {
//        LeaseResponse lease = leaseService.getLeaseById(id);
//        if (lease != null) {
//            return ResponseEntity.ok(lease);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // UPDATE (Frontend → /leases/update/{id})
//    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> updateLease(@PathVariable Long id, @ModelAttribute LeaseRequest leaseRequest) {
//        try {
//            System.out.println("Received lease update request for ID: " + id);
//            System.out.println("CompanyName: " + leaseRequest.getCompanyName());
//            System.out.println("TenantType: " + leaseRequest.getTenantType());
//            System.out.println("TenantTrade: " + leaseRequest.getTenantTrade());
//            System.out.println("NRC: " + leaseRequest.getNrc());
//            System.out.println("Address: " + leaseRequest.getAddress());
//            System.out.println("Heir: " + leaseRequest.getHeir());
//
//            LeaseResponse updatedLease = leaseService.updateLease(id, leaseRequest);
//            if (updatedLease != null) {
//                return ResponseEntity.ok(updatedLease);
//            }
//            return ResponseEntity.notFound().build();
//        } catch (MultipartException e) {
//            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().body("Error updating lease: " + e.getMessage());
//        }
//    }
//
//    // DELETE (Frontend → /leases/delete/{id})
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
//        if (leaseService.deleteLease(id)) {
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//
//    // Get lease with full details including space info
//    @GetMapping("/my-leases/{id}/details")
//    public ResponseEntity<LeaseResponse> getMyLeaseDetails(@PathVariable Long id, Principal principal) {
//        try {
//            LeaseResponse lease = leaseService.getLeaseWithDetails(id);
//
//            // Verify the lease belongs to the current user
//            String username = principal.getName();
//            User user = userService.findUserByProfile(username);
//
//            if (!lease.getTenantId().equals(user.getId().toString())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//            }
//
//            return ResponseEntity.ok(lease);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Get leases for current tenant user
//    @GetMapping("/my-leases")
//    public ResponseEntity<List<LeaseResponse>> getMyLeases(Principal principal) {
//        try {
//            List<LeaseResponse> leases = leaseService.getLeasesForCurrentUser(principal);
//            return ResponseEntity.ok(leases);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // In LeaseController.java - FIX THIS METHOD
//    @GetMapping("/my-spaces")
//    public ResponseEntity<List<SpaceResponseDTO>> getMyLeasedSpaces(Principal principal) {
//        try {
//            System.out.println("🔐 Current user: " + principal.getName());
//
//            // Get current user using the principal name
//            User user = userService.findUserByProfile(principal.getName());
//
//            if (user == null) {
//                System.out.println("❌ User not found for: " + principal.getName());
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            System.out.println("✅ User found: " + user.getEmail() + ", ID: " + user.getId());
//
//            // Get leases for this user
//            List<Lease> userLeases = leaseService.getLeasesByTenantId(user.getId().toString());
//            System.out.println("📋 User leases count: " + userLeases.size());
//
//            // Extract spaces from leases
//            List<SpaceResponseDTO> leasedSpaces = userLeases.stream()
//                    .map(lease -> {
//                        try {
//                            if (lease.getSpace() == null) {
//                                System.out.println("❌ Lease " + lease.getLeaseId() + " has no space");
//                                return null;
//                            }
//                            SpaceResponseDTO space = spaceService.getSpaceById(lease.getSpace().getSpaceId());
//                            System.out.println("🏢 Space found: " + lease.getSpace().getSpaceId() + " - " + (space != null ? "exists" : "null"));
//                            return space;
//                        } catch (Exception e) {
//                            System.out.println("❌ Error getting space for lease: " + lease.getLeaseId());
//                            e.printStackTrace();
//                            return null;
//                        }
//                    })
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            System.out.println("✅ Final leased spaces count: " + leasedSpaces.size());
//            return new ResponseEntity<>(leasedSpaces, HttpStatus.OK);
//        } catch (Exception e) {
//            System.err.println("💥 Error in getMyLeasedSpaces:");
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Get leases for current tenant in specific branch
//    @GetMapping("/my-leases/branch/{branchId}")
//    public ResponseEntity<List<LeaseResponse>> getMyLeasesByBranch(
//            @PathVariable Long branchId,
//            Principal principal) {
//        try {
//            System.out.println("🔐 Getting leases for branch: " + branchId + ", user: " + principal.getName());
//
//            // Get current user
//            User user = userService.findUserByProfile(principal.getName());
//
//            if (user == null) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            // Use the repository method directly
//            List<Lease> userLeases = leaseService.getLeasesByTenantIdAndBranchId(user.getId().toString(), branchId);
//
//            // Convert to responses
//            List<LeaseResponse> leaseResponses = userLeases.stream()
//                    .map(LeaseResponse::new)
//                    .collect(Collectors.toList());
//
//            System.out.println("✅ Final leases in branch " + branchId + ": " + leaseResponses.size());
//            return new ResponseEntity<>(leaseResponses, HttpStatus.OK);
//        } catch (Exception e) {
//            System.err.println("💥 Error in getMyLeasesByBranch:");
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Add to LeaseController.java
//    @GetMapping("/debug/leases")
//    public ResponseEntity<List<Map<String, Object>>> debugLeases(Principal principal) {
//        try {
//            User user = userService.findUserByProfile(principal.getName());
//            if (user == null) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            List<Lease> userLeases = leaseService.getLeasesByTenantId(user.getId().toString());
//            List<Map<String, Object>> leaseDetails = new ArrayList<>();
//
//            for (Lease lease : userLeases) {
//                Map<String, Object> details = new HashMap<>();
//                details.put("leaseId", lease.getLeaseId());
//                details.put("tenantId", lease.getTenant() != null ? lease.getTenant().getId() : "null");
//                details.put("space", lease.getSpace() != null ? lease.getSpace().getSpaceId() : "null");
//                details.put("spaceObject", lease.getSpace()); // This will show if space is properly loaded
//                leaseDetails.add(details);
//            }
//
//            return new ResponseEntity<>(leaseDetails, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}


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
