// AuditReportController.java
package com.sein_gar_har.reportController;

import com.sein_gar_har.reportService.AuditReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditReportController {

    private final AuditReportService auditReportService;

    @GetMapping("/user-activity")
    public ResponseEntity<byte[]> generateUserActivityReport(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Generating user activity report for user: {}, from: {} to: {}", username, startDate, endDate);

        try {
            byte[] report = auditReportService.generateUserActivityReport(username, startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename",
                    "user-activity-" + username + "-" + LocalDateTime.now() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating user activity report for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Unexpected error generating user activity report for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/login-activity")
    public ResponseEntity<byte[]> generateLoginActivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Generating login activity report from: {} to: {}", startDate, endDate);

        try {
            byte[] report = auditReportService.generateLoginActivityReport(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename",
                    "login-activity-" + LocalDateTime.now() + ".pdf");
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating login activity report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Unexpected error generating login activity report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/role-changes")
    public ResponseEntity<byte[]> generateRoleChangeAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Generating role change audit report from: {} to: {}", startDate, endDate);

        try {
            byte[] report = auditReportService.generateRoleChangeAuditReport(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename",
                    "role-changes-audit-" + LocalDateTime.now() + ".pdf");
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating role change audit report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Unexpected error generating role change audit report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<byte[]> generateComprehensiveAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Generating comprehensive audit report from: {} to: {}", startDate, endDate);

        try {
            byte[] report = auditReportService.generateComprehensiveAuditReport(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename",
                    "comprehensive-audit-" + LocalDateTime.now() + ".pdf");
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating comprehensive audit report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Unexpected error generating comprehensive audit report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }
}