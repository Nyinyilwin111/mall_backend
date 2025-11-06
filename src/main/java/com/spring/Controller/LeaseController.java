
package com.spring.Controller;

import com.spring.DTO.request.LeaseRequest;
import com.spring.DTO.response.LeaseResponse;
import com.spring.Entity.Lease;
import com.spring.Services.LeaseService;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/leases")
public class LeaseController {

    @Autowired
    private LeaseService leaseService;

    @PostMapping("/create")
    public ResponseEntity<?> createLease(@ModelAttribute LeaseRequest leaseRequest) {
        try {
            Lease lease = leaseService.createLease(leaseRequest);
            return ResponseEntity.ok(lease);
        } catch (MultipartException e) {
            return ResponseEntity.badRequest().body("File upload failed!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllLeases() {
        try {
            List<LeaseResponse> leases = leaseService.getAllLeases();
            return ResponseEntity.ok(leases);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @GetMapping("/get/{leaseId}")
    public ResponseEntity<?> getLeaseById(@PathVariable Long leaseId) {
        try {
            LeaseResponse lease = leaseService.getLeaseById(leaseId);  // return DTO not entity
            return ResponseEntity.ok(lease);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    // NEW: Update lease
    @PutMapping("/update/{leaseId}")
    public ResponseEntity<?> updateLease(@PathVariable Long leaseId, @ModelAttribute LeaseRequest leaseRequest) {
        try {
            LeaseResponse lease = leaseService.updateLease(leaseId, leaseRequest);
            return ResponseEntity.ok(lease);
        } catch (MultipartException e) {
            return ResponseEntity.badRequest().body("File upload failed!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    // NEW: Delete lease
    @DeleteMapping("/delete/{leaseId}")
    public ResponseEntity<?> deleteLease(@PathVariable Long leaseId) {
        try {
            leaseService.deleteLease(leaseId);
            return ResponseEntity.ok().body("Lease deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
