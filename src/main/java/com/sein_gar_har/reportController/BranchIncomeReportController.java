package com.sein_gar_har.reportController;

import com.sein_gar_har.dto.report.BranchIncomeReportRequest;
import com.sein_gar_har.dto.report.PreviewRequest;
import com.sein_gar_har.reportService.BranchIncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.sein_gar_har.reportService.BranchIncomeService.ReportFormat.*;

@Slf4j
@RestController
@RequestMapping("/api/reports/branchesIncome")
@RequiredArgsConstructor
public class BranchIncomeReportController {

    private final BranchIncomeService reportService;

    // Add this new endpoint for preview data
    @PostMapping("/preview")
    public ResponseEntity<?> getPreviewData(@Valid @RequestBody PreviewRequest request) {
        try {
            log.info("Generating preview data for period type: {}, selected date: {}, branch: {}",
                    request.getPeriodType(), request.getSelectedDate(), request.getBranchId());

            // Calculate date range based on period type
            LocalDate[] dateRange = calculateDateRange(request.getSelectedDate(), request.getPeriodType());
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            // Generate preview data
            Map<String, Object> previewData = reportService.generatePreviewData(startDate, endDate, request.getPeriodType(), request.getBranchId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", previewData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating preview data", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error generating preview: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/branch-income/pdf")
    public ResponseEntity<byte[]> generatePdfReport(@Valid @RequestBody BranchIncomeReportRequest request) {
        return generateReport(request, PDF);
    }

    @PostMapping("/branch-income/excel")
    public ResponseEntity<byte[]> generateExcelReport(@Valid @RequestBody BranchIncomeReportRequest request) {
        return generateReport(request, EXCEL);
    }

    @PostMapping("/branch-income/csv")
    public ResponseEntity<byte[]> generateCsvReport(@Valid @RequestBody BranchIncomeReportRequest request) {
        return generateReport(request, CSV);
    }

    // Generic endpoint that uses reportFormat from request
    @PostMapping("/branch-income")
    public ResponseEntity<byte[]> generateReport(@Valid @RequestBody BranchIncomeReportRequest request) {
        try {
            String format = request.getReportFormat().toUpperCase();
            BranchIncomeService.ReportFormat reportFormat = BranchIncomeService.ReportFormat.valueOf(format);
            return generateReport(request, reportFormat);
        } catch (IllegalArgumentException e) {
            log.error("Invalid report format: {}", request.getReportFormat());
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<byte[]> generateReport(BranchIncomeReportRequest request,
                                                  BranchIncomeService.ReportFormat format) {
        try {
            validateRequest(request);

            byte[] reportContent = switch (format) {
                case PDF -> reportService.generatePdfReport(
                        request.getStartDate(), request.getEndDate(),
                        request.getPeriodType(), request.getBranchId());
                case EXCEL -> reportService.generateExcelReport(
                        request.getStartDate(), request.getEndDate(),
                        request.getPeriodType(), request.getBranchId());
                case CSV -> reportService.generateCsvReport(
                        request.getStartDate(), request.getEndDate(),
                        request.getPeriodType(), request.getBranchId());
            };

            String filename = generateFilename(request, format);
            MediaType mediaType = getMediaType(format);

            log.info("Successfully generated {} report for period {} to {}",
                    format, request.getStartDate(), request.getEndDate());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(mediaType)
                    .body(reportContent);

        } catch (Exception e) {
            log.error("Error generating {} report", format, e);
            return ResponseEntity.internalServerError().body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }

    private void validateRequest(BranchIncomeReportRequest request) {
        if (!request.isDateRangeValid()) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (!request.isStartDateValid()) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
        if (!request.isDateRangeWithinLimit()) {
            throw new IllegalArgumentException("Date range cannot exceed 2 years");
        }
    }

    private String generateFilename(BranchIncomeReportRequest request, BranchIncomeService.ReportFormat format) {
        String periodType = request.getPeriodType() != null ? request.getPeriodType().toLowerCase() : "monthly";
        String branchInfo = request.getBranchId() != null ? "-branch-" + request.getBranchId() : "";

        return String.format("branch-income-%s-report%s-%s.%s",
                periodType, branchInfo, LocalDate.now(), format.name().toLowerCase());
    }

    private MediaType getMediaType(BranchIncomeService.ReportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
        };
    }

    // Helper method to calculate date range based on period type
    private LocalDate[] calculateDateRange(LocalDate selectedDate, String periodType) {
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }

        switch (periodType.toUpperCase()) {
            case "DAILY":
                return new LocalDate[]{selectedDate, selectedDate};
            case "MONTHLY":
                LocalDate firstDayOfMonth = LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), 1);
                LocalDate lastDayOfMonth = LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), selectedDate.lengthOfMonth());
                return new LocalDate[]{firstDayOfMonth, lastDayOfMonth};
            case "YEARLY":
                LocalDate firstDayOfYear = LocalDate.of(selectedDate.getYear(), 1, 1);
                LocalDate lastDayOfYear = LocalDate.of(selectedDate.getYear(), 12, 31);
                return new LocalDate[]{firstDayOfYear, lastDayOfYear};
            default:
                return new LocalDate[]{selectedDate, selectedDate};
        }
    }
}