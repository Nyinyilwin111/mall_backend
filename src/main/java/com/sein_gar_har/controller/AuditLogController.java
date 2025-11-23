// AuditLogController.java
package com.sein_gar_har.controller;

import com.sein_gar_har.RepositoryAudit.AuditLogRepository;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.auditEntity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditlogs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository repo;
    @Autowired
    AuditLogService auditLogService;

    @GetMapping
    // @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')") // Temporarily remove security for testing
    public List<AuditLog> getAllAuditLogs() {
        return repo.findAllByOrderByTimestampDesc();
    }
    // Add this method to your AuditLogController.java
    @PostMapping("/custom-log")
    public ResponseEntity<?> createCustomAuditLog(@RequestBody Map<String, Object> logRequest) {
        try {
            String action = (String) logRequest.get("action");
            String message = (String) logRequest.get("message");
            Object details = logRequest.get("details");

            auditLogService.logCustomAction(action, message, details);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating custom audit log: " + e.getMessage());
        }
    }
}