package com.sein_gar_har.reportService;

import java.time.LocalDate;
import java.util.Map;

public interface SpaceIncomeService {

    Map<String, Object> generatePreviewData(LocalDate startDate, LocalDate endDate,
                                            String periodType, String spaceCode, String branchId);

    byte[] generatePdfReport(LocalDate startDate, LocalDate endDate,
                             String periodType, String spaceCode, String branchId);

    byte[] generateExcelReport(LocalDate startDate, LocalDate endDate,
                               String periodType, String spaceCode, String branchId);

    byte[] generateCsvReport(LocalDate startDate, LocalDate endDate,
                             String periodType, String spaceCode, String branchId);

    enum ReportFormat {
        PDF, EXCEL, CSV
    }
}