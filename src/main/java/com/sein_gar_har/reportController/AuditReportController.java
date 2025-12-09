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
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditReportController {

    private final AuditReportService auditReportService;
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ResponseEntity<byte[]> createErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body(message.getBytes());
    }

    private String generateFileName(String reportType, String username, String format) {
        String baseName;
        switch (reportType) {
            case "user-activity":
                String safeUsername = username != null ? username.replaceAll("[^a-zA-Z0-9.-]", "_") : "all";
                baseName = String.format("user-activity-%s-%s",
                        safeUsername,
                        LocalDateTime.now().format(FILE_DATE_FORMATTER));
                break;
            case "login-activity":
                baseName = String.format("login-activity-%s",
                        LocalDateTime.now().format(FILE_DATE_FORMATTER));
                break;
            case "role-changes":
                baseName = String.format("role-changes-audit-%s",
                        LocalDateTime.now().format(FILE_DATE_FORMATTER));
                break;
            case "comprehensive":
                baseName = String.format("comprehensive-audit-%s",
                        LocalDateTime.now().format(FILE_DATE_FORMATTER));
                break;
            default:
                baseName = String.format("audit-report-%s",
                        LocalDateTime.now().format(FILE_DATE_FORMATTER));
        }

        return "excel".equalsIgnoreCase(format) ? baseName + ".xlsx" : baseName + ".pdf";
    }

    @GetMapping("/user-activity")
    public ResponseEntity<byte[]> generateUserActivityReport(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "pdf") String format) {
        System.out.print("reach in backend ------------------");
        log.info("Generating user activity report for user: {}, from: {} to: {}, format: {}",
                username, startDate, endDate, format);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Start date must be before end date".getBytes());
        }

        // Validate format
        if (!"pdf".equalsIgnoreCase(format) && !"excel".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Format must be either 'pdf' or 'excel'".getBytes());
        }

        try {
            byte[] report;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format)) {
                report = auditReportService.generateUserActivityReportExcel(username, startDate, endDate);
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                report = auditReportService.generateUserActivityReport(username, startDate, endDate);
                contentType = MediaType.APPLICATION_PDF;
            }

            String fileName = generateFileName("user-activity", username, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("filename", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating user activity report for user: {}", username, e);
            return createErrorResponse("Error generating report: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating user activity report for user: {}", username, e);
            return createErrorResponse("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/login-activity")
    public ResponseEntity<byte[]> generateLoginActivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "pdf") String format) {

        log.info("Generating login activity report from: {} to: {}, format: {}", startDate, endDate, format);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Start date must be before end date".getBytes());
        }

        // Validate format
        if (!"pdf".equalsIgnoreCase(format) && !"excel".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Format must be either 'pdf' or 'excel'".getBytes());
        }

        try {
            byte[] report;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format)) {
                report = auditReportService.generateLoginActivityReportExcel(startDate, endDate);
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                report = auditReportService.generateLoginActivityReport(startDate, endDate);
                contentType = MediaType.APPLICATION_PDF;
            }

            String fileName = generateFileName("login-activity", null, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("filename", fileName);
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating login activity report", e);
            return createErrorResponse("Error generating report: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating login activity report", e);
            return createErrorResponse("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/role-changes")
    public ResponseEntity<byte[]> generateRoleChangeAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "pdf") String format) {

        log.info("Generating role change audit report from: {} to: {}, format: {}", startDate, endDate, format);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Start date must be before end date".getBytes());
        }

        // Validate format
        if (!"pdf".equalsIgnoreCase(format) && !"excel".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Format must be either 'pdf' or 'excel'".getBytes());
        }

        try {
            byte[] report;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format)) {
                report = auditReportService.generateRoleChangeAuditReportExcel(startDate, endDate);
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                report = auditReportService.generateRoleChangeAuditReport(startDate, endDate);
                contentType = MediaType.APPLICATION_PDF;
            }

            String fileName = generateFileName("role-changes", null, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("filename", fileName);
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating role change audit report", e);
            return createErrorResponse("Error generating report: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating role change audit report", e);
            return createErrorResponse("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<byte[]> generateComprehensiveAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "pdf") String format) {

        log.info("Generating comprehensive audit report from: {} to: {}, format: {}", startDate, endDate, format);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Start date must be before end date".getBytes());
        }

        // Validate format
        if (!"pdf".equalsIgnoreCase(format) && !"excel".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Format must be either 'pdf' or 'excel'".getBytes());
        }

        try {
            byte[] report;
            MediaType contentType;

            if ("excel".equalsIgnoreCase(format)) {
                report = auditReportService.generateComprehensiveAuditReportExcel(startDate, endDate);
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                report = auditReportService.generateComprehensiveAuditReport(startDate, endDate);
                contentType = MediaType.APPLICATION_PDF;
            }

            String fileName = generateFileName("comprehensive", null, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDispositionFormData("filename", fileName);
            headers.setContentLength(report.length);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException e) {
            log.error("Error generating comprehensive audit report", e);
            return createErrorResponse("Error generating report: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating comprehensive audit report", e);
            return createErrorResponse("Unexpected error: " + e.getMessage());
        }
    }
}