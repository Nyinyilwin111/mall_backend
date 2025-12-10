//// SpaceIncomeServiceImpl.java - CORRECTED TABLE NAMES
//package com.sein_gar_har.reportService.ReportServiceImpl;
//
//import com.sein_gar_har.reportService.SpaceIncomeService;
//import com.sein_gar_har.reportService.reportUtil.ReportCompiler;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jasperreports.engine.*;
//import net.sf.jasperreports.engine.export.JRCsvExporter;
//import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
//import net.sf.jasperreports.export.*;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//import java.io.ByteArrayOutputStream;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SpaceIncomeServiceImpl implements SpaceIncomeService {
//
//    private final DataSource dataSource;
//    private final ReportCompiler reportCompiler;
//
//    private static final String SPACE_INCOME_REPORT = "space_income_report";
//
//    @Override
//    public Map<String, Object> generatePreviewData(LocalDate startDate, LocalDate endDate,
//                                                  String periodType, String spaceCode, String branchId) {
//        Map<String, Object> previewData = new HashMap<>();
//
//        try (Connection connection = dataSource.getConnection()) {
//            log.info("Generating space income preview: {} to {}, period: {}, space: {}, branch: {}",
//                    startDate, endDate, periodType, spaceCode, branchId);
//
//            // 1. Get Total Summary - FIXED TABLE NAME: spacetype
//            String summaryQuery = """
//                SELECT
//                    COUNT(DISTINCT s.space_code) as total_spaces,
//                    COUNT(*) as total_payments,
//                    COALESCE(SUM(p.amount), 0) as total_amount,
//                    COALESCE(AVG(p.amount), 0) as average_payment
//                FROM payment p
//                LEFT JOIN lease l ON p.lease_id = l.lease_id
//                LEFT JOIN utility u ON p.utility_id = u.utility_id
//                LEFT JOIN space s ON l.space_id = s.space_id OR u.space_id = s.space_id
//                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
//                LEFT JOIN branches b ON f.branch_branch_id = b.id
//                WHERE p.status IN ('PAID', 'VERIFIED')
//                AND p.payment_date BETWEEN ? AND ?
//                AND (? IS NULL OR s.space_code = ?)
//                AND (? IS NULL OR b.id = ?)
//                """;
//
//            try (PreparedStatement stmt = connection.prepareStatement(summaryQuery)) {
//                int paramIndex = 1;
//                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
//                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
//
//                // Handle spaceCode (String)
//                if (spaceCode != null && !spaceCode.isEmpty()) {
//                    stmt.setString(paramIndex++, spaceCode);
//                    stmt.setString(paramIndex++, spaceCode);
//                } else {
//                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
//                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
//                }
//
//                // Handle branchId (String -> Long conversion)
//                if (branchId != null && !branchId.isEmpty()) {
//                    try {
//                        Long branchIdLong = Long.parseLong(branchId);
//                        stmt.setLong(paramIndex++, branchIdLong);
//                        stmt.setLong(paramIndex, branchIdLong);
//                    } catch (NumberFormatException e) {
//                        stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
//                        stmt.setNull(paramIndex, java.sql.Types.BIGINT);
//                    }
//                } else {
//                    stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
//                    stmt.setNull(paramIndex, java.sql.Types.BIGINT);
//                }
//
//                ResultSet rs = stmt.executeQuery();
//                if (rs.next()) {
//                    previewData.put("totalSpaces", rs.getInt("total_spaces"));
//                    previewData.put("totalPayments", rs.getInt("total_payments"));
//                    previewData.put("totalAmount", rs.getBigDecimal("total_amount"));
//                    previewData.put("averagePayment", rs.getBigDecimal("average_payment"));
//                }
//            }
//
//            // 2. Payment Type Breakdown
//            String typeQuery = """
//                SELECT
//                    CASE
//                        WHEN p.lease_id IS NOT NULL THEN 'LEASE'
//                        WHEN p.utility_id IS NOT NULL THEN 'UTILITY'
//                        ELSE 'OTHER'
//                    END as payment_type,
//                    COUNT(*) as type_count,
//                    COALESCE(SUM(p.amount), 0) as type_amount
//                FROM payment p
//                LEFT JOIN lease l ON p.lease_id = l.lease_id
//                LEFT JOIN utility u ON p.utility_id = u.utility_id
//                LEFT JOIN space s ON l.space_id = s.space_id OR u.space_id = s.space_id
//                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
//                LEFT JOIN branches b ON f.branch_branch_id = b.id
//                WHERE p.status IN ('PAID', 'VERIFIED')
//                AND p.payment_date BETWEEN ? AND ?
//                AND (? IS NULL OR s.space_code = ?)
//                AND (? IS NULL OR b.id = ?)
//                GROUP BY
//                    CASE
//                        WHEN p.lease_id IS NOT NULL THEN 'LEASE'
//                        WHEN p.utility_id IS NOT NULL THEN 'UTILITY'
//                        ELSE 'OTHER'
//                    END
//                """;
//
//            try (PreparedStatement stmt = connection.prepareStatement(typeQuery)) {
//                int paramIndex = 1;
//                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
//                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
//
//                if (spaceCode != null && !spaceCode.isEmpty()) {
//                    stmt.setString(paramIndex++, spaceCode);
//                    stmt.setString(paramIndex++, spaceCode);
//                } else {
//                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
//                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
//                }
//
//                if (branchId != null && !branchId.isEmpty()) {
//                    try {
//                        Long branchIdLong = Long.parseLong(branchId);
//                        stmt.setLong(paramIndex++, branchIdLong);
//                        stmt.setLong(paramIndex, branchIdLong);
//                    } catch (NumberFormatException e) {
//                        stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
//                        stmt.setNull(paramIndex, java.sql.Types.BIGINT);
//                    }
//                } else {
//                    stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
//                    stmt.setNull(paramIndex, java.sql.Types.BIGINT);
//                }
//
//                ResultSet rs = stmt.executeQuery();
//                Map<String, Object> paymentTypes = new HashMap<>();
//                while (rs.next()) {
//                    Map<String, Object> typeData = new HashMap<>();
//                    typeData.put("count", rs.getInt("type_count"));
//                    typeData.put("amount", rs.getBigDecimal("type_amount"));
//                    paymentTypes.put(rs.getString("payment_type"), typeData);
//                }
//                previewData.put("paymentTypes", paymentTypes);
//            }
//
//            // 3. Top Performing Spaces - FIXED: Changed space_type to spacetype
//            String topSpacesQuery = """
//                SELECT
//                    s.space_code,
//                    s.location,
//                    st.type_name as space_type,
//                    COALESCE(SUM(p.amount), 0) as total_income,
//                    COUNT(*) as payment_count
//                FROM payment p
//                LEFT JOIN lease l ON p.lease_id = l.lease_id
//                LEFT JOIN utility u ON p.utility_id = u.utility_id
//                LEFT JOIN space s ON l.space_id = s.space_id OR u.space_id = s.space_id
//                LEFT JOIN spacetype st ON s.space_type_id = st.space_type_id  -- FIXED: spacetype table
//                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
//                LEFT JOIN branches b ON f.branch_branch_id = b.id
//                WHERE p.status IN ('PAID', 'VERIFIED')
//                AND p.payment_date BETWEEN ? AND ?
//                AND (? IS NULL OR b.id = ?)
//                GROUP BY s.space_code, s.location, st.type_name
//                ORDER BY total_income DESC
//                LIMIT 10
//                """;
//
//            try (PreparedStatement stmt = connection.prepareStatement(topSpacesQuery)) {
//                int paramIndex = 1;
//                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
//                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
//
//                if (branchId != null && !branchId.isEmpty()) {
//                    try {
//                        Long branchIdLong = Long.parseLong(branchId);
//                        stmt.setLong(paramIndex++, branchIdLong);
//                        stmt.setLong(paramIndex, branchIdLong);
//                    } catch (NumberFormatException e) {
//                        stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
//                        stmt.setNull(paramIndex, java.sql.Types.BIGINT);
//                    }
//                } else {
//                    stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
//                    stmt.setNull(paramIndex, java.sql.Types.BIGINT);
//                }
//
//                ResultSet rs = stmt.executeQuery();
//                Map<String, Object> topSpaces = new HashMap<>();
//                while (rs.next()) {
//                    Map<String, Object> spaceData = new HashMap<>();
//                    spaceData.put("location", rs.getString("location"));
//                    spaceData.put("type", rs.getString("space_type"));
//                    spaceData.put("income", rs.getBigDecimal("total_income"));
//                    spaceData.put("payments", rs.getInt("payment_count"));
//                    topSpaces.put(rs.getString("space_code"), spaceData);
//                }
//                previewData.put("topSpaces", topSpaces);
//            }
//
//            log.info("✅ Space income preview data generated successfully");
//
//        } catch (Exception e) {
//            log.error("Error generating space income preview data", e);
//            throw new RuntimeException("Failed to generate preview data", e);
//        }
//
//        return previewData;
//    }
//
//    @Override
//    public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate,
//                                   String periodType, String spaceCode, String branchId) {
//        return generateReport(startDate, endDate, periodType, spaceCode, branchId, ReportFormat.PDF);
//    }
//
//    @Override
//    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate,
//                                     String periodType, String spaceCode, String branchId) {
//        return generateReport(startDate, endDate, periodType, spaceCode, branchId, ReportFormat.EXCEL);
//    }
//
//    @Override
//    public byte[] generateCsvReport(LocalDate startDate, LocalDate endDate,
//                                   String periodType, String spaceCode, String branchId) {
//        return generateReport(startDate, endDate, periodType, spaceCode, branchId, ReportFormat.CSV);
//    }
//
//    private byte[] generateReport(LocalDate startDate, LocalDate endDate, String periodType,
//                                 String spaceCode, String branchId, ReportFormat format) {
//        try (Connection connection = dataSource.getConnection()) {
//
//            // Adjust dates for period type
//            LocalDate adjustedStartDate = adjustStartDateForPeriod(startDate, periodType);
//            LocalDate adjustedEndDate = adjustEndDateForPeriod(endDate, periodType);
//
//            log.info("Generating {} space income report: {} to {}, period: {}, space: {}, branch: {}",
//                    format, adjustedStartDate, adjustedEndDate, periodType, spaceCode, branchId);
//
//            // Get compiled report
//            JasperReport jasperReport = reportCompiler.getCompiledReport(SPACE_INCOME_REPORT);
//
//            // Create parameters with proper type conversion
//            Map<String, Object> parameters = createReportParameters(adjustedStartDate, adjustedEndDate,
//                                                                   periodType, spaceCode, branchId);
//
//            // Fill report
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
//
//            // Export based on format
//            switch (format) {
//                case PDF:
//                    return JasperExportManager.exportReportToPdf(jasperPrint);
//                case EXCEL:
//                    return exportToExcel(jasperPrint);
//                case CSV:
//                    return exportToCsv(jasperPrint);
//                default:
//                    throw new IllegalArgumentException("Unsupported format: " + format);
//            }
//
//        } catch (Exception e) {
//            log.error("Error generating space income report", e);
//            throw new RuntimeException("Failed to generate report", e);
//        }
//    }
//
//    private LocalDate adjustStartDateForPeriod(LocalDate date, String periodType) {
//        if (date == null || periodType == null) return date;
//
//        switch (periodType.toUpperCase()) {
//            case "YEARLY":
//                return LocalDate.of(date.getYear(), 1, 1);
//            case "MONTHLY":
//                return LocalDate.of(date.getYear(), date.getMonth(), 1);
//            case "DAILY":
//            default:
//                return date;
//        }
//    }
//
//    private LocalDate adjustEndDateForPeriod(LocalDate date, String periodType) {
//        if (date == null || periodType == null) return date;
//
//        switch (periodType.toUpperCase()) {
//            case "YEARLY":
//                return LocalDate.of(date.getYear(), 12, 31);
//            case "MONTHLY":
//                return LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
//            case "DAILY":
//            default:
//                return date;
//        }
//    }
//
//    private Map<String, Object> createReportParameters(LocalDate startDate, LocalDate endDate,
//                                                      String periodType, String spaceCode, String branchId) {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("reportTitle", "Space Income Report");
//        parameters.put("startDate", java.sql.Date.valueOf(startDate));
//        parameters.put("endDate", java.sql.Date.valueOf(endDate));
//        parameters.put("periodType", periodType);
//        parameters.put("spaceCode", spaceCode);
//        parameters.put("generatedDate", new java.util.Date());
//        parameters.put("companyName", "Sein Gay Har");
//        parameters.put("generatedBy", "System Administrator");
//        parameters.put("generatedAt", java.time.LocalDateTime.now().toString());
//
//        // CRITICAL FIX: Convert branchId from String to Long
//        if (branchId != null && !branchId.isEmpty()) {
//            try {
//                parameters.put("branchId", Long.parseLong(branchId));
//            } catch (NumberFormatException e) {
//                parameters.put("branchId", null); // Set to null if invalid
//            }
//        } else {
//            parameters.put("branchId", null);
//        }
//
//        log.debug("Report parameters: {}", parameters);
//        return parameters;
//    }
//
//    private byte[] exportToExcel(JasperPrint jasperPrint) throws JRException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        JRXlsxExporter exporter = new JRXlsxExporter();
//        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
//
//        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
//        configuration.setOnePagePerSheet(false);
//        configuration.setRemoveEmptySpaceBetweenRows(true);
//        configuration.setDetectCellType(true);
//        configuration.setWhitePageBackground(false);
//
//        exporter.setConfiguration(configuration);
//        exporter.exportReport();
//
//        return outputStream.toByteArray();
//    }
//
//    private byte[] exportToCsv(JasperPrint jasperPrint) throws JRException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        JRCsvExporter exporter = new JRCsvExporter();
//        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
//
//        SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
//        configuration.setFieldDelimiter(",");
//
//        exporter.setConfiguration(configuration);
//        exporter.exportReport();
//
//        return outputStream.toByteArray();
//    }
//}
// SpaceIncomeServiceImpl.java - COMPLETE FIXED VERSION
package com.sein_gar_har.reportService.ReportServiceImpl;

import com.sein_gar_har.reportService.SpaceIncomeService;
import com.sein_gar_har.reportService.reportUtil.ReportCompiler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceIncomeServiceImpl implements SpaceIncomeService {

    private final DataSource dataSource;
    private final ReportCompiler reportCompiler;

    private static final String SPACE_INCOME_REPORT = "space_income_report";

    @Override
    public Map<String, Object> generatePreviewData(LocalDate startDate, LocalDate endDate,
                                                   String periodType, String spaceCode, String branchId) {
        Map<String, Object> previewData = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            log.info("Generating space income preview: {} to {}, period: {}, space: {}, branch: {}",
                    startDate, endDate, periodType, spaceCode, branchId);

            // ✅ FIXED: Get detailed income breakdown by space and payment type
            String detailedQuery = """
                SELECT 
                    s.space_id,
                    s.space_code,
                    st.type_name as space_type,
                    f.level as floor_level,
                    b.name as branch_name,
                    SUM(CASE WHEN p.lease_id IS NOT NULL THEN p.amount ELSE 0 END) as lease_income,
                    SUM(CASE WHEN p.utility_id IS NOT NULL THEN p.amount ELSE 0 END) as utility_income,
                    COUNT(DISTINCT p.payment_id) as payment_count
                FROM payment p
                LEFT JOIN lease l ON p.lease_id = l.lease_id
                LEFT JOIN utility u ON p.utility_id = u.utility_id
                LEFT JOIN space s ON (l.space_id = s.space_id OR u.space_id = s.space_id)
                LEFT JOIN spacetype st ON s.space_type_id = st.space_type_id
                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
                LEFT JOIN branches b ON f.branch_branch_id = b.id
                WHERE p.status IN ('PAID', 'VERIFIED')
                AND p.payment_date BETWEEN ? AND ?
                AND (? IS NULL OR s.space_code = ?)
                AND (? IS NULL OR b.id = ?)
                GROUP BY s.space_id, s.space_code, st.type_name, f.level, b.name
                ORDER BY s.space_code
                """;

            List<Map<String, Object>> spacesData = new ArrayList<>();
            double totalLeaseIncome = 0;
            double totalUtilityIncome = 0;
            int totalSpaces = 0;
            int totalPayments = 0;

            try (PreparedStatement stmt = connection.prepareStatement(detailedQuery)) {
                int paramIndex = 1;
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));

                // Handle spaceCode
                if (spaceCode != null && !spaceCode.isEmpty()) {
                    stmt.setString(paramIndex++, spaceCode);
                    stmt.setString(paramIndex++, spaceCode);
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                }

                // Handle branchId
                if (branchId != null && !branchId.isEmpty()) {
                    try {
                        Long branchIdLong = Long.parseLong(branchId);
                        stmt.setLong(paramIndex++, branchIdLong);
                        stmt.setLong(paramIndex, branchIdLong);
                    } catch (NumberFormatException e) {
                        stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
                        stmt.setNull(paramIndex, java.sql.Types.BIGINT);
                    }
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.BIGINT);
                    stmt.setNull(paramIndex, java.sql.Types.BIGINT);
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> spaceData = new HashMap<>();

                    double leaseIncome = rs.getDouble("lease_income");
                    double utilityIncome = rs.getDouble("utility_income");
                    double totalIncome = leaseIncome + utilityIncome;

                    spaceData.put("space_id", rs.getString("space_id"));
                    spaceData.put("space_code", rs.getString("space_code"));
                    spaceData.put("space_type", rs.getString("space_type"));
                    spaceData.put("floor_level", rs.getString("floor_level"));
                    spaceData.put("branch_name", rs.getString("branch_name"));
                    spaceData.put("lease_income", leaseIncome);
                    spaceData.put("utility_income", utilityIncome);
                    spaceData.put("total_income", totalIncome);
                    spaceData.put("payment_count", rs.getInt("payment_count"));

                    spacesData.add(spaceData);

                    totalLeaseIncome += leaseIncome;
                    totalUtilityIncome += utilityIncome;
                    totalSpaces++;
                    totalPayments += rs.getInt("payment_count");
                }
            }

            // ✅ Calculate totals
            double totalAmount = totalLeaseIncome + totalUtilityIncome;
            double averagePayment = totalPayments > 0 ? totalAmount / totalPayments : 0;

            // ✅ Payment type breakdown
            Map<String, Object> paymentTypes = new HashMap<>();

            Map<String, Object> leaseType = new HashMap<>();
            leaseType.put("count", (int) (totalLeaseIncome > 0 ? Math.ceil(totalLeaseIncome / 100000) : 0));
            leaseType.put("amount", totalLeaseIncome);
            paymentTypes.put("LEASE", leaseType);

            Map<String, Object> utilityType = new HashMap<>();
            utilityType.put("count", (int) (totalUtilityIncome > 0 ? Math.ceil(totalUtilityIncome / 5000) : 0));
            utilityType.put("amount", totalUtilityIncome);
            paymentTypes.put("UTILITY", utilityType);

            // ✅ Top performing spaces
            Map<String, Object> topSpaces = new HashMap<>();
            spacesData.stream()
                    .sorted((a, b) -> Double.compare(
                            (double) b.get("total_income"),
                            (double) a.get("total_income")
                    ))
                    .limit(10)
                    .forEach(space -> {
                        String spaceCodeVal = (String) space.get("space_code");
                        Map<String, Object> spaceDetails = new HashMap<>();
                        spaceDetails.put("type", space.get("space_type"));
                        spaceDetails.put("income", space.get("total_income"));
                        spaceDetails.put("payments", space.get("payment_count"));
                        spaceDetails.put("lease_income", space.get("lease_income"));
                        spaceDetails.put("utility_income", space.get("utility_income"));
                        topSpaces.put(spaceCodeVal, spaceDetails);
                    });

            // ✅ Set preview data
            previewData.put("totalSpaces", totalSpaces);
            previewData.put("totalPayments", totalPayments);
            previewData.put("totalAmount", totalAmount);
            previewData.put("totalLeaseIncome", totalLeaseIncome);
            previewData.put("totalUtilityIncome", totalUtilityIncome);
            previewData.put("averagePayment", averagePayment);
            previewData.put("paymentTypes", paymentTypes);
            previewData.put("topSpaces", topSpaces);
            previewData.put("spacesData", spacesData);

            log.info("✅ Space income preview data generated successfully. " +
                            "Lease: {}, Utility: {}, Total: {}",
                    totalLeaseIncome, totalUtilityIncome, totalAmount);

        } catch (Exception e) {
            log.error("Error generating space income preview data", e);
            throw new RuntimeException("Failed to generate preview data", e);
        }

        return previewData;
    }

    @Override
    public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate,
                                    String periodType, String spaceCode, String branchId) {
        return generateReport(startDate, endDate, periodType, spaceCode, branchId, ReportFormat.PDF);
    }

    @Override
    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate,
                                      String periodType, String spaceCode, String branchId) {
        return generateReport(startDate, endDate, periodType, spaceCode, branchId, ReportFormat.EXCEL);
    }

    @Override
    public byte[] generateCsvReport(LocalDate startDate, LocalDate endDate,
                                    String periodType, String spaceCode, String branchId) {
        return generateReport(startDate, endDate, periodType, spaceCode, branchId, ReportFormat.CSV);
    }

    private byte[] generateReport(LocalDate startDate, LocalDate endDate, String periodType,
                                  String spaceCode, String branchId, ReportFormat format) {
        try (Connection connection = dataSource.getConnection()) {

            // Adjust dates for period type
            LocalDate adjustedStartDate = adjustStartDateForPeriod(startDate, periodType);
            LocalDate adjustedEndDate = adjustEndDateForPeriod(endDate, periodType);

            log.info("Generating {} space income report: {} to {}, period: {}, space: {}, branch: {}",
                    format, adjustedStartDate, adjustedEndDate, periodType, spaceCode, branchId);

            // Get compiled report
            JasperReport jasperReport = reportCompiler.getCompiledReport(SPACE_INCOME_REPORT);

            // Create parameters with proper type conversion
            Map<String, Object> parameters = createReportParameters(adjustedStartDate, adjustedEndDate,
                    periodType, spaceCode, branchId);

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // Export based on format
            switch (format) {
                case PDF:
                    return JasperExportManager.exportReportToPdf(jasperPrint);
                case EXCEL:
                    return exportToExcel(jasperPrint);
                case CSV:
                    return exportToCsv(jasperPrint);
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }

        } catch (Exception e) {
            log.error("Error generating space income report", e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private LocalDate adjustStartDateForPeriod(LocalDate date, String periodType) {
        if (date == null || periodType == null) return date;

        switch (periodType.toUpperCase()) {
            case "YEARLY":
                return LocalDate.of(date.getYear(), 1, 1);
            case "MONTHLY":
                return LocalDate.of(date.getYear(), date.getMonth(), 1);
            case "DAILY":
            default:
                return date;
        }
    }

    private LocalDate adjustEndDateForPeriod(LocalDate date, String periodType) {
        if (date == null || periodType == null) return date;

        switch (periodType.toUpperCase()) {
            case "YEARLY":
                return LocalDate.of(date.getYear(), 12, 31);
            case "MONTHLY":
                return LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
            case "DAILY":
            default:
                return date;
        }
    }

    private Map<String, Object> createReportParameters(LocalDate startDate, LocalDate endDate,
                                                       String periodType, String spaceCode, String branchId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "Space Income Report");
        parameters.put("startDate", java.sql.Date.valueOf(startDate));
        parameters.put("endDate", java.sql.Date.valueOf(endDate));
        parameters.put("periodType", periodType);
        parameters.put("spaceCode", spaceCode);
        parameters.put("generatedDate", new java.util.Date());
        parameters.put("companyName", "Sein Gay Har");
        parameters.put("generatedBy", "System Administrator");
        parameters.put("generatedAt", java.time.LocalDateTime.now().toString());

        // ✅ Add MMK conversion rate
//        parameters.put("exchangeRate", 2100.0);
//        parameters.put("currencySymbol", "MMK");
        // ✅ REMOVED exchange rate since we're working directly in MMK
        parameters.put("currencySymbol", "MMK");
        parameters.put("currencyCode", "MMK");

        // CRITICAL FIX: Convert branchId from String to Long
        if (branchId != null && !branchId.isEmpty()) {
            try {
                parameters.put("branchId", Long.parseLong(branchId));
            } catch (NumberFormatException e) {
                parameters.put("branchId", null);
            }
        } else {
            parameters.put("branchId", null);
        }

        log.debug("Report parameters: {}", parameters);
        return parameters;
    }

    private byte[] exportToExcel(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setDetectCellType(true);
        configuration.setWhitePageBackground(false);

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return outputStream.toByteArray();
    }

    private byte[] exportToCsv(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

        SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
        configuration.setFieldDelimiter(",");

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return outputStream.toByteArray();
    }
}