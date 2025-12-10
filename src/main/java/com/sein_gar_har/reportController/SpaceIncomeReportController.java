package com.sein_gar_har.reportController;

import com.sein_gar_har.dto.report.SpaceIncomeReportRequest;
import com.sein_gar_har.reportService.SpaceIncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports/space-income")
@RequiredArgsConstructor
public class SpaceIncomeReportController {

    private final SpaceIncomeService spaceIncomeService;

    @PostMapping("/preview")
    public ResponseEntity<?> getPreviewData(@Valid @RequestBody SpaceIncomeReportRequest request) {
        try {
            log.info("Generating space income preview data: {} to {}, period: {}",
                    request.getStartDate(), request.getEndDate(), request.getPeriodType());

            // Convert Long branchId to String properly
            String branchIdStr = null;
            if (request.getBranchId() != null) {
                branchIdStr = String.valueOf(request.getBranchId());
            }

            Map<String, Object> previewData = spaceIncomeService.generatePreviewData(
                    request.getStartDate(), request.getEndDate(),
                    request.getPeriodType(), request.getSpaceCode(),
                    branchIdStr
            );

            return ResponseEntity.ok(Map.of("success", true, "data", previewData));

        } catch (Exception e) {
            log.error("Error generating preview data", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(@Valid @RequestBody SpaceIncomeReportRequest request) {
        return generateReport(request, SpaceIncomeService.ReportFormat.PDF);
    }

    @PostMapping("/excel")
    public ResponseEntity<byte[]> generateExcelReport(@Valid @RequestBody SpaceIncomeReportRequest request) {
        return generateReport(request, SpaceIncomeService.ReportFormat.EXCEL);
    }

    @PostMapping("/csv")
    public ResponseEntity<byte[]> generateCsvReport(@Valid @RequestBody SpaceIncomeReportRequest request) {
        return generateReport(request, SpaceIncomeService.ReportFormat.CSV);
    }

    @PostMapping
    public ResponseEntity<byte[]> generateReport(@Valid @RequestBody SpaceIncomeReportRequest request) {
        try {
            String format = request.getReportFormat().toUpperCase();
            SpaceIncomeService.ReportFormat reportFormat = SpaceIncomeService.ReportFormat.valueOf(format);
            return generateReport(request, reportFormat);
        } catch (IllegalArgumentException e) {
            log.error("Invalid report format: {}", request.getReportFormat());
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<byte[]> generateReport(SpaceIncomeReportRequest request,
                                                  SpaceIncomeService.ReportFormat format) {
        try {
            // Validate request
            if (!request.isDateRangeValid()) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }

            if (!request.isDateRangeWithinLimit()) {
                throw new IllegalArgumentException("Date range cannot exceed 2 years");
            }

            // Generate report
            byte[] reportContent;

            // Convert Long branchId to String properly
            String branchIdStr = null;
            if (request.getBranchId() != null) {
                branchIdStr = String.valueOf(request.getBranchId());
            }

            switch (format) {
                case PDF:
                    reportContent = spaceIncomeService.generatePdfReport(
                            request.getStartDate(), request.getEndDate(),
                            request.getPeriodType(), request.getSpaceCode(), branchIdStr);
                    break;
                case EXCEL:
                    reportContent = spaceIncomeService.generateExcelReport(
                            request.getStartDate(), request.getEndDate(),
                            request.getPeriodType(), request.getSpaceCode(), branchIdStr);
                    break;
                case CSV:
                    reportContent = spaceIncomeService.generateCsvReport(
                            request.getStartDate(), request.getEndDate(),
                            request.getPeriodType(), request.getSpaceCode(), branchIdStr);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }

            // Create filename
            String filename = createFilename(request, format);
            MediaType mediaType = getMediaType(format);

            log.info("✅ Space income report generated: {}", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(mediaType)
                    .body(reportContent);

        } catch (Exception e) {
            log.error("Error generating space income report", e);
            return ResponseEntity.internalServerError()
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    private String createFilename(SpaceIncomeReportRequest request, SpaceIncomeService.ReportFormat format) {
        StringBuilder filename = new StringBuilder("space-income-report");

        if (request.getSpaceCode() != null) {
            filename.append("-").append(request.getSpaceCode());
        }

        if (request.getBranchId() != null) {
            filename.append("-branch-").append(request.getBranchId());
        }

        filename.append("-").append(request.getPeriodType().toLowerCase())
                .append("-").append(LocalDate.now())
                .append(".").append(format.name().toLowerCase());

        return filename.toString();
    }

    private MediaType getMediaType(SpaceIncomeService.ReportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
        };
    }
}