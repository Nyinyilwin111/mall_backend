package com.sein_gar_har.controller.reportController;

import com.sein_gar_har.reportService.SpaceReportService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports/spaces")
// REMOVE THIS: @CrossOrigin(origins = "*")
public class SpaceReportController {

    @Autowired
    private SpaceReportService spaceReportService;

    @RequestMapping(value = "/{spaceCode}/detail", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handlePreflight(@PathVariable String spaceCode) {
        log.info("Handling preflight request for spaceCode: {}", spaceCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<byte[]> generateSpaceListReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String spaceType,
            @RequestParam(required = false) String branch) {

        try {
            Map<String, Object> parameters = new HashMap<>();
            if (status != null) parameters.put("status", status);
            if (spaceType != null) parameters.put("spaceType", spaceType);
            if (branch != null) parameters.put("branch", branch);

            byte[] reportContent = spaceReportService.generateSpaceListReport(parameters);

            return createPdfResponse(reportContent, "space-list-report.pdf");

        } catch (JRException e) {
            log.error("Error generating space list report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/{spaceCode}/detail")
    public ResponseEntity<byte[]> generateSpaceDetailReport(@PathVariable String spaceCode) {
        try {
            log.info("🔵 Generating space detail report for spaceCode: {}", spaceCode);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("spaceCode", spaceCode);

            byte[] reportContent = spaceReportService.generateSpaceDetailReport(spaceCode, parameters);

            if (reportContent == null || reportContent.length == 0) {
                log.warn("Empty report content for spaceCode: {}", spaceCode);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("✅ Successfully generated report for spaceCode: {}, size: {} bytes", spaceCode, reportContent.length);
            return createPdfResponse(reportContent, "space-detail-" + spaceCode + ".pdf");

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
            @RequestParam(required = false) String status) {

        try {
            Map<String, Object> parameters = new HashMap<>();
            if (status != null) parameters.put("status", status);

            byte[] reportContent = spaceReportService.generateSpaceAvailabilityReport(parameters);

            return createPdfResponse(reportContent, "space-availability-report.pdf");

        } catch (JRException e) {
            log.error("Error generating space availability report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/floor/{floorId}")
    public ResponseEntity<byte[]> generateSpaceByFloorReport(@PathVariable Integer floorId) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("floorId", floorId);

            byte[] reportContent = spaceReportService.generateSpaceByFloorReport(floorId, parameters);

            return createPdfResponse(reportContent, "floor-" + floorId + "-spaces.pdf");

        } catch (JRException e) {
            log.error("Error generating space by floor report for floorId: {}", floorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] content, String filename) {
        if (content == null || content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}