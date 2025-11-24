package com.sein_gar_har.controller;

import com.sein_gar_har.Services.MaintenanceRequestService;
import com.sein_gar_har.Services.AuditLogService; // ✅ ADD
import com.sein_gar_har.dto.request.MaintenanceRequestDTO;
import com.sein_gar_har.dto.response.MaintenanceRequestResponse;
import lombok.RequiredArgsConstructor; // ✅ ADD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/maintenance-requests")
@RequiredArgsConstructor
public class MaintenanceRequestController {

    @Autowired
    MaintenanceRequestService maintenanceRequestService;

    @Autowired
    AuditLogService auditLogService;

    // Tenant submit maintenance request
    @PostMapping
    public ResponseEntity<MaintenanceRequestResponse> createRequest(
            @RequestBody MaintenanceRequestDTO requestDTO) {
        MaintenanceRequestResponse response = maintenanceRequestService.createRequest(requestDTO);
        return ResponseEntity.ok(response);
    }

    // Manager: view all requests
    @GetMapping
    public ResponseEntity<List<MaintenanceRequestResponse>> getAllRequests() {
        List<MaintenanceRequestResponse> response = maintenanceRequestService.getAllRequests();
        response.forEach(item -> System.out.println("mmmmm item : " + item));
        return ResponseEntity.ok(response);

    }

    // Tenant or Manager: get by ID
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponse> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(maintenanceRequestService.getRequestById(id));
    }

    // Tenant: view own requests
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<MaintenanceRequestResponse>> getByTenant(
            @PathVariable("tenantId") UUID tenantId) {
        return ResponseEntity.ok(maintenanceRequestService.getRequestsByTenant(tenantId));
    }

    // Manager: update full request (assign staff, edit)
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponse> updateRequest(
            @PathVariable("id") UUID id,
            @RequestBody MaintenanceRequestDTO requestDTO) {
        return ResponseEntity.ok(maintenanceRequestService.updateRequest(id, requestDTO));
    }

    // FR-6.2 & FR-6.3: Update status (Pending/In_Progress/Resolved/Cancelled)
    @PatchMapping("/{id}/status")
    public ResponseEntity<MaintenanceRequestResponse> updateStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status) {
        return ResponseEntity.ok(maintenanceRequestService.updateStatus(id, status));
    }

    // DELETE endpoint with audit log
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("id") UUID id) {
        maintenanceRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    // Custom audit log endpoint for maintenance actions
    @PostMapping("/{id}/audit-log")
    public ResponseEntity<?> createMaintenanceAuditLog(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> logRequest) {
        try {
            String action = (String) logRequest.get("action");
            String message = (String) logRequest.get("message");
            Object details = logRequest.get("details");

            // Add maintenance request context to details
            if (details instanceof Map) {
                ((Map<String, Object>) details).put("maintenanceRequestId", id.toString());
            }

            auditLogService.logCustomAction(action, message, details);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating maintenance audit log: " + e.getMessage());
        }
    }
}