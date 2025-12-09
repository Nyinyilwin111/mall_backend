package com.sein_gar_har.reportController;

import com.sein_gar_har.reportService.SpaceReportService;
import com.sein_gar_har.reportService.reportUtil.ReportCompiler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/reports/spaces")
public class SpaceReportController {

    @Autowired
    private SpaceReportService spaceReportService;

     @Autowired
     private ReportCompiler reportCompiler;

    @RequestMapping(value = "/{spaceCode}/detail", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handlePreflight(@PathVariable String spaceCode) {
        log.info("Handling preflight request for spaceCode: {}", spaceCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/list")
    public ResponseEntity<byte[]> generateSpaceListReport(
            @RequestBody(required = false) Map<String, Object> filterParams,
            @RequestParam(defaultValue = "pdf") String format) {

        try {
            log.info("🔍 Received POST request for space list report");
            log.info("🔍 Request body (filterParams): {}", filterParams);
            log.info("🔍 Format: {}", format);

            Map<String, Object> parameters = new HashMap<>();

            // Extract filters from JSON body
            if (filterParams != null) {
                log.info("🔍 Processing filter parameters:");

                // Support both status formats
                if (filterParams.containsKey("status")) {
                    Object statusValue = filterParams.get("status");
                    String statusStr = statusValue != null ? statusValue.toString().toUpperCase() : null;
                    parameters.put("status", statusStr);
                    log.info("✅ Added status filter: {}", statusStr);
                }

                // Support both spaceType and spaceTypeId
                if (filterParams.containsKey("spaceTypeId")) {
                    Object spaceTypeIdValue = filterParams.get("spaceTypeId");
                    parameters.put("spaceTypeId", spaceTypeIdValue);
                    log.info("✅ Added spaceTypeId filter: {}", spaceTypeIdValue);
                }
                if (filterParams.containsKey("spaceType")) {
                    Object spaceTypeValue = filterParams.get("spaceType");
                    parameters.put("spaceType", spaceTypeValue);
                    log.info("✅ Added spaceType filter: {}", spaceTypeValue);
                }

                // Support both branch and branchId
                if (filterParams.containsKey("branchId")) {
                    Object branchIdValue = filterParams.get("branchId");
                    parameters.put("branchId", branchIdValue);
                    log.info("✅ Added branchId filter: {}", branchIdValue);
                }
                if (filterParams.containsKey("branch")) {
                    Object branchValue = filterParams.get("branch");
                    parameters.put("branch", branchValue);
                    log.info("✅ Added branch filter: {}", branchValue);
                }
            }

            log.info("🎯 Final parameters sent to Jasper: {}", parameters);

            byte[] reportContent = spaceReportService.generateSpaceListReport(parameters, format);

            log.info("✅ Report generated successfully, size: {} bytes", reportContent.length);

            return createResponse(reportContent, "space-list-report", format);

        } catch (JRException e) {
            log.error("❌ Error generating space list report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("❌ Unexpected error generating space list report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/{spaceCode}/detail")
    public ResponseEntity<byte[]> generateSpaceDetailReport(
            @PathVariable String spaceCode,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            log.info("🔵 Generating space detail report for spaceCode: {}, format: {}", spaceCode, format);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("spaceCode", spaceCode);

            byte[] reportContent = spaceReportService.generateSpaceDetailReport(spaceCode, parameters, format);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for spaceCode: {}", spaceCode);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated report for spaceCode: {}, format: {}, size: {} bytes",
                    spaceCode, format, reportContent.length);
            return createResponse(reportContent, "space-detail-" + spaceCode, format);

        } catch (JRException e) {
            log.error("❌ Error generating space detail report for spaceCode: {}", spaceCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("❌ Unexpected error generating space detail report for spaceCode: {}", spaceCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<byte[]> generateSpaceAvailabilityReport(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "pdf") String format) {

        try {
            Map<String, Object> parameters = new HashMap<>();
            if (status != null) parameters.put("status", status);

            byte[] reportContent = spaceReportService.generateSpaceAvailabilityReport(parameters, format);

            return createResponse(reportContent, "space-availability-report", format);

        } catch (JRException e) {
            log.error("Error generating space availability report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Unexpected error generating space availability report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/floor/{floorId}")
    public ResponseEntity<byte[]> generateSpaceByFloorReport(
            @PathVariable Integer floorId,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("floorId", floorId);

            byte[] reportContent = spaceReportService.generateSpaceByFloorReport(floorId, parameters, format);

            return createResponse(reportContent, "floor-" + floorId + "-spaces", format);

        } catch (JRException e) {
            log.error("Error generating space by floor report for floorId: {}", floorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("Unexpected error generating space by floor report for floorId: {}", floorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
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
            case "word":
            case "doc":
                headers.setContentType(MediaType.parseMediaType("application/msword"));
                break;
            case "docx":
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
                break;
            default:
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        headers.setContentDispositionFormData("filename", fullFilename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
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
            case "word":
            case "doc":
                return "doc";
            case "docx":
                return "docx";
            default:
                return "pdf";
        }
    }

    @GetMapping("/{spaceCode}/detail/two-page")
    public ResponseEntity<byte[]> generateTwoPageSpaceDetailReport(
            @PathVariable String spaceCode,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            log.info("🔵 Generating TWO-PAGE space detail report for spaceCode: {}, format: {}", spaceCode, format);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("spaceCode", spaceCode);

            byte[] reportContent = spaceReportService.generateTwoPageSpaceDetailReport(spaceCode, parameters, format);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for two-page report, spaceCode: {}", spaceCode);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated TWO-PAGE report for spaceCode: {}, format: {}, size: {} bytes",
                    spaceCode, format, reportContent.length);
            return createResponse(reportContent, "space-detail-two-page-" + spaceCode, format);

        } catch (JRException e) {
            log.error("❌ Error generating two-page space detail report for spaceCode: {}", spaceCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating two-page report: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("❌ Unexpected error generating two-page space detail report for spaceCode: {}", spaceCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }
    // Add this to your SpaceReportController.java

    @GetMapping("/validate/jrxml")
    public ResponseEntity<Map<String, Object>> validateJrxmlFile() {
        Map<String, Object> response = new HashMap<>();

        try {
            String reportPath = "reports/spaces/space_list_report.jrxml";
            InputStream stream = new ClassPathResource(reportPath).getInputStream();
            String content = new String(stream.readAllBytes());
            stream.close();

            // Check for duplicate parameters
            Map<String, Integer> paramCounts = new HashMap<>();
            String[] lines = content.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains("<parameter name=")) {
                    // Extract parameter name
                    int start = line.indexOf("name=\"") + 6;
                    int end = line.indexOf("\"", start);
                    if (start > 5 && end > start) {
                        String paramName = line.substring(start, end);
                        paramCounts.put(paramName, paramCounts.getOrDefault(paramName, 0) + 1);
                    }
                }
            }

            List<String> duplicateParams = new ArrayList<>();
            paramCounts.forEach((param, count) -> {
                if (count > 1) {
                    duplicateParams.add(param + " (appears " + count + " times)");
                }
            });

            response.put("fileName", "space_list_report.jrxml");
            response.put("fileSize", content.length());
            response.put("lineCount", lines.length);
            response.put("totalParameters", paramCounts.size());
            response.put("duplicateParameters", duplicateParams);
            response.put("hasDuplicates", !duplicateParams.isEmpty());
            response.put("status", duplicateParams.isEmpty() ? "VALID" : "INVALID");

            if (!duplicateParams.isEmpty()) {
                response.put("message", "Found duplicate parameters in JRXML file");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                response.put("message", "JRXML file is valid");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to validate JRXML file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Add this to your SpaceReportController.java

    @PostMapping("/cleanup/cache")
    public ResponseEntity<Map<String, String>> cleanupReportCache() {
        Map<String, String> response = new HashMap<>();

        try {
            reportCompiler.clearAllCache();
            response.put("status", "SUCCESS");
            response.put("message", "All report caches cleared successfully");
            response.put("timestamp", new Date().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to clear cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}