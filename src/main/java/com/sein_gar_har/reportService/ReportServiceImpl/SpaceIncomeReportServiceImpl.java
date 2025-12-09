//package com.sein_gar_har.reportService.ReportServiceImpl;
//
//import com.sein_gar_har.reportService.SpaceIncomeReportService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jasperreports.engine.*;
//import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ResourceUtils;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SpaceIncomeReportServiceImpl implements SpaceIncomeReportService {
//
//    private final SpaceIncomeReportRepository spaceIncomeReportRepository;
//
//    @Override
//    public List<SpaceIncomeDTO> getSpaceIncomeData(SpaceIncomeRequestDTO request) {
//        log.info("Fetching space income data from {} to {} for branchId: {}, spaceId: {}",
//                request.getStartDate(), request.getEndDate(), request.getBranchId(), request.getSpaceId());
//
//        return spaceIncomeReportRepository.getSpaceIncomeReport(
//                request.getStartDate(),
//                request.getEndDate(),
//                request.getBranchId(),
//                request.getSpaceId()
//        );
//    }
//
//    @Override
//    public SpaceIncomeSummaryDTO getSpaceIncomeSummary(SpaceIncomeRequestDTO request) {
//        log.info("Fetching space income summary from {} to {} for branchId: {}, spaceId: {}",
//                request.getStartDate(), request.getEndDate(), request.getBranchId(), request.getSpaceId());
//
//        return spaceIncomeReportRepository.getSpaceIncomeSummary(
//                request.getStartDate(),
//                request.getEndDate(),
//                request.getBranchId(),
//                request.getSpaceId()
//        );
//    }
//
//    @Override
//    public byte[] generatePdfReport(SpaceIncomeRequestDTO request) throws JRException, IOException {
//        log.info("Generating PDF report for space income");
//
//        // Get data
//        List<SpaceIncomeDTO> spaceIncomeData = getSpaceIncomeData(request);
//        SpaceIncomeSummaryDTO summary = getSpaceIncomeSummary(request);
//
//        // Load JRXML template
//        InputStream jrxmlStream = new ClassPathResource("reports/space-income-report.jrxml").getInputStream();
//
//        // Compile report
//        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
//
//        // Create data source
//        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(spaceIncomeData);
//
//        // Set parameters
//        Map<String, Object> parameters = getReportParameters(request);
//
//        // Fill report
//        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
//
//        // Export to PDF
//        return JasperExportManager.exportReportToPdf(jasperPrint);
//    }
//
//    @Override
//    public Map<String, Object> getReportParameters(SpaceIncomeRequestDTO request) {
//        Map<String, Object> parameters = new HashMap<>();
//
//        // Set request parameters
//        parameters.put("reportTitle", request.getReportTitle() != null ?
//                request.getReportTitle() : "Space Income Report");
//        parameters.put("startDate", Date.valueOf(request.getStartDate()));
//        parameters.put("endDate", Date.valueOf(request.getEndDate()));
//        parameters.put("branchId", request.getBranchId());
//        parameters.put("spaceId", request.getSpaceId());
//        parameters.put("periodType", request.getPeriodType() != null ?
//                request.getPeriodType() : "CUSTOM");
//        parameters.put("generatedBy", request.getGeneratedBy() != null ?
//                request.getGeneratedBy() : "System");
//        parameters.put("generatedAt", LocalDate.now().toString());
//        parameters.put("COMPANY_NAME", "Sein Gar Har Real Estate");
//        parameters.put("REPORT_DATE", new java.util.Date());
//
//        // Load company logo
//        try {
//            InputStream logoStream = new ClassPathResource("image/SGH-logo.png").getInputStream();
//            parameters.put("COMPANY_LOGO", logoStream);
//        } catch (IOException e) {
//            log.warn("Company logo not found, using default");
//        }
//
//        return parameters;
//    }
//
//    // Additional helper methods
//    public Map<String, Object> getSpaceIncomeStatistics(SpaceIncomeRequestDTO request) {
//        List<SpaceIncomeDTO> data = getSpaceIncomeData(request);
//        SpaceIncomeSummaryDTO summary = getSpaceIncomeSummary(request);
//
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("totalSpaces", summary.getTotalSpaces());
//        stats.put("totalPayments", summary.getTotalPayments());
//        stats.put("totalIncome", summary.getTotalIncome());
//        stats.put("averageIncome", summary.getAverageIncome());
//        stats.put("topPerformingSpaces", getTopPerformingSpaces(data, 5));
//        stats.put("spaceCountByType", getSpaceCountByType(data));
//        stats.put("incomeByBranch", getIncomeByBranch(data));
//
//        return stats;
//    }
//
//    private List<SpaceIncomeDTO> getTopPerformingSpaces(List<SpaceIncomeDTO> data, int limit) {
//        return data.stream()
//                .sorted((a, b) -> b.getTotalIncome().compareTo(a.getTotalIncome()))
//                .limit(limit)
//                .toList();
//    }
//
//    private Map<String, Long> getSpaceCountByType(List<SpaceIncomeDTO> data) {
//        return data.stream()
//                .collect(java.util.stream.Collectors.groupingBy(
//                        SpaceIncomeDTO::getSpaceType,
//                        java.util.stream.Collectors.counting()
//                ));
//    }
//
//    private Map<String, java.math.BigDecimal> getIncomeByBranch(List<SpaceIncomeDTO> data) {
//        return data.stream()
//                .collect(java.util.stream.Collectors.groupingBy(
//                        SpaceIncomeDTO::getBranchName,
//                        java.util.stream.Collectors.reducing(
//                                java.math.BigDecimal.ZERO,
//                                SpaceIncomeDTO::getTotalIncome,
//                                java.math.BigDecimal::add
//                        )
//                ));
//    }
//}