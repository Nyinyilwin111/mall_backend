package com.sein_gar_har.reportService;

import java.time.LocalDate;
import java.util.Map;

public interface BranchIncomeService {

    // Add this method signature
    Map<String, Object> generatePreviewData(LocalDate startDate, LocalDate endDate, String periodType, Long branchId);

    byte[] generatePdfReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId);

    byte[] generateExcelReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId);

    byte[] generateBranchIncomeReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId);

    byte[] generateCsvReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId);

    enum ReportFormat {
        PDF, EXCEL, CSV
    }

    class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ReportGenerationException(String message) {
            super(message);
        }
    }
}