package com.sein_gar_har.reportController;

import com.sein_gar_har.reportService.BranchReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reports/branches")
public class BranchReportController {

    @Autowired
    private BranchReportService branchReportService;

    // ✅ Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("🔵 Health check for branch reports");
        return ResponseEntity.ok("Branch Reports Service is running");
    }

    @GetMapping("/list")
    public ResponseEntity<byte[]> generateBranchListReport(
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            log.info("🔵 Generating branch list report, format: {}", format);

            byte[] reportContent = branchReportService.generateBranchListReport(format);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for branch list");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated branch list report, size: {} bytes", reportContent.length);
            return createResponse(reportContent, "branch-list-report", format);

        } catch (Exception e) {
            log.error("❌ Error generating branch list report", e);
            return createErrorResponse("Error generating report: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{branchId}/detail")
    public ResponseEntity<byte[]> generateBranchDetailReport(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            log.info("🔵 Generating branch detail report for branch ID: {}, format: {}", branchId, format);

            // ✅ Validate input
            if (branchId == null || branchId <= 0) {
                return createErrorResponse("Invalid branch ID: " + branchId, HttpStatus.BAD_REQUEST);
            }

            byte[] reportContent = branchReportService.generateBranchDetailReport(branchId, format);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for branch detail, branchId: {}", branchId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated branch detail report for branch ID: {}, size: {} bytes",
                    branchId, reportContent.length);
            return createResponse(reportContent, "branch-detail-" + branchId, format);

        } catch (Exception e) {
            log.error("❌ Error generating branch detail report for branch ID: {}", branchId, e);
            return createErrorResponse("Error generating report: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{branchId}/users")
    public ResponseEntity<byte[]> generateBranchUsersReport(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            log.info("🔵 Generating branch users report for branch ID: {}, format: {}", branchId, format);

            // ✅ Validate input
            if (branchId == null || branchId <= 0) {
                return createErrorResponse("Invalid branch ID: " + branchId, HttpStatus.BAD_REQUEST);
            }

            byte[] reportContent = branchReportService.generateBranchUsersReport(branchId, format);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for branch users, branchId: {}", branchId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated branch users report for branch ID: {}, size: {} bytes",
                    branchId, reportContent.length);
            return createResponse(reportContent, "branch-users-" + branchId, format);

        } catch (Exception e) {
            log.error("❌ Error generating branch users report for branch ID: {}", branchId, e);
            return createErrorResponse("Error generating report: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<byte[]> generateBranchAnalyticsReport(
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            log.info("🔵 Generating branch analytics report, format: {}", format);

            byte[] reportContent = branchReportService.generateBranchAnalyticsReport(format);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for branch analytics");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated branch analytics report, size: {} bytes", reportContent.length);
            return createResponse(reportContent, "branch-analytics-report", format);

        } catch (Exception e) {
            log.error("❌ Error generating branch analytics report", e);
            return createErrorResponse("Error generating report: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<byte[]> createResponse(byte[] content, String filename, String format) {
        if (content == null || content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        String fileExtension = getFileExtension(format);
        String fullFilename = filename + "." + fileExtension;

        switch (format.toLowerCase()) {
            case "pdf":
                headers.setContentType(MediaType.APPLICATION_PDF);
                break;
            case "excel":
            case "xls":
                headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
                break;
            case "xlsx":
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                break;
            default:
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        headers.setContentDispositionFormData("filename", fullFilename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    // ✅ Add this missing method
    private ResponseEntity<byte[]> createErrorResponse(String errorMessage, HttpStatus status) {
        log.error("❌ Error response: {}", errorMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(errorMessage.getBytes(), headers, status);
    }

    private String getFileExtension(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "pdf";
            case "excel":
            case "xls":
                return "xls";
            case "xlsx":
                return "xlsx";
            default:
                return "pdf";
        }
    }
}