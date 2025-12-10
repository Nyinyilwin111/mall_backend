package com.sein_gar_har.reportService.ReportServiceImpl;

import com.sein_gar_har.reportService.BranchIncomeService;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchIncomeServiceImpl implements BranchIncomeService {

    private final DataSource dataSource;
    private final ReportCompiler reportCompiler;

    private static final String BRANCH_INCOME_REPORT = "branch_income_report";

    // Add this new method for preview data
    @Override
    public Map<String, Object> generatePreviewData(LocalDate startDate, LocalDate endDate, String periodType, Long branchId) {
        Map<String, Object> previewData = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            log.info("Generating preview data from {} to {}, period: {}, branch: {}",
                    startDate, endDate, periodType, branchId);

            // Get total payments and amount
            String summaryQuery = """
                SELECT 
                    COUNT(*) as total_payments,
                    COALESCE(SUM(p.amount), 0) as total_amount,
                    COUNT(DISTINCT b.id) as branch_count
                FROM payment p
                LEFT JOIN lease l ON p.lease_id = l.lease_id
                LEFT JOIN utility u ON p.utility_id = u.utility_id
                LEFT JOIN space s ON l.space_id = s.space_id OR u.space_id = s.space_id
                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
                LEFT JOIN branches b ON f.branch_branch_id = b.id
                WHERE p.status IN ('PAID', 'VERIFIED')
                AND p.payment_date BETWEEN ? AND ?
                AND (? IS NULL OR b.id = ?)
                """;

            try (PreparedStatement stmt = connection.prepareStatement(summaryQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(startDate));
                stmt.setDate(2, java.sql.Date.valueOf(endDate));
                stmt.setObject(3, branchId);
                stmt.setObject(4, branchId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    previewData.put("totalPayments", rs.getInt("total_payments"));
                    previewData.put("totalAmount", rs.getBigDecimal("total_amount"));
                    previewData.put("branchCount", rs.getInt("branch_count"));
                }
            }

            // Get payment type breakdown
            String typeQuery = """
                SELECT 
                    CASE 
                        WHEN p.lease_id IS NOT NULL THEN 'LEASE'
                        WHEN p.utility_id IS NOT NULL THEN 'UTILITY'
                        ELSE 'OTHER'
                    END as payment_type,
                    COUNT(*) as type_count,
                    COALESCE(SUM(p.amount), 0) as type_amount
                FROM payment p
                LEFT JOIN lease l ON p.lease_id = l.lease_id
                LEFT JOIN utility u ON p.utility_id = u.utility_id
                LEFT JOIN space s ON l.space_id = s.space_id OR u.space_id = s.space_id
                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
                LEFT JOIN branches b ON f.branch_branch_id = b.id
                WHERE p.status IN ('PAID', 'VERIFIED')
                AND p.payment_date BETWEEN ? AND ?
                AND (? IS NULL OR b.id = ?)
                GROUP BY 
                    CASE 
                        WHEN p.lease_id IS NOT NULL THEN 'LEASE'
                        WHEN p.utility_id IS NOT NULL THEN 'UTILITY'
                        ELSE 'OTHER'
                    END
                """;

            try (PreparedStatement stmt = connection.prepareStatement(typeQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(startDate));
                stmt.setDate(2, java.sql.Date.valueOf(endDate));
                stmt.setObject(3, branchId);
                stmt.setObject(4, branchId);

                ResultSet rs = stmt.executeQuery();
                Map<String, Object> paymentTypes = new HashMap<>();
                while (rs.next()) {
                    Map<String, Object> typeData = new HashMap<>();
                    typeData.put("count", rs.getInt("type_count"));
                    typeData.put("amount", rs.getBigDecimal("type_amount"));
                    paymentTypes.put(rs.getString("payment_type"), typeData);
                }
                previewData.put("paymentTypes", paymentTypes);
            }

            // Get branch breakdown
            String branchQuery = """
                SELECT 
                    COALESCE(b.name, 'No Branch') as branch_name,
                    COUNT(*) as branch_payments,
                    COALESCE(SUM(p.amount), 0) as branch_amount
                FROM payment p
                LEFT JOIN lease l ON p.lease_id = l.lease_id
                LEFT JOIN utility u ON p.utility_id = u.utility_id
                LEFT JOIN space s ON l.space_id = s.space_id OR u.space_id = s.space_id
                LEFT JOIN floor f ON s.Floor_floor_id = f.floor_id
                LEFT JOIN branches b ON f.branch_branch_id = b.id
                WHERE p.status IN ('PAID', 'VERIFIED')
                AND p.payment_date BETWEEN ? AND ?
                AND (? IS NULL OR b.id = ?)
                GROUP BY b.id, b.name
                ORDER BY branch_amount DESC
                """;

            try (PreparedStatement stmt = connection.prepareStatement(branchQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(startDate));
                stmt.setDate(2, java.sql.Date.valueOf(endDate));
                stmt.setObject(3, branchId);
                stmt.setObject(4, branchId);

                ResultSet rs = stmt.executeQuery();
                Map<String, Object> branchesData = new HashMap<>();
                while (rs.next()) {
                    Map<String, Object> branchData = new HashMap<>();
                    branchData.put("paymentCount", rs.getInt("branch_payments"));
                    branchData.put("amount", rs.getBigDecimal("branch_amount"));
                    branchesData.put(rs.getString("branch_name"), branchData);
                }
                previewData.put("branches", branchesData);
            }

            // Add period info
            previewData.put("periodInfo", generatePeriodInfo(startDate, endDate, periodType));
            previewData.put("dateRange", startDate + " to " + endDate);

            log.info("✅ Preview data generated successfully");

        } catch (Exception e) {
            log.error("Error generating preview data", e);
            throw new ReportGenerationException("Failed to generate preview data", e);
        }

        return previewData;
    }

    private String generatePeriodInfo(LocalDate startDate, LocalDate endDate, String periodType) {
        switch (periodType.toUpperCase()) {
            case "DAILY":
                return "Daily Report for " + startDate;
            case "MONTHLY":
                return "Monthly Report for " + startDate.getMonth() + " " + startDate.getYear();
            case "YEARLY":
                return "Yearly Report for " + startDate.getYear();
            default:
                return "Report from " + startDate + " to " + endDate;
        }
    }

    @Override
    public byte[] generateBranchIncomeReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId) {
        return generatePdfReport(startDate, endDate, periodType, branchId);
    }

    @Override
    public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId) {
        return generateReport(startDate, endDate, periodType, branchId, ReportFormat.PDF);
    }

    @Override
    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId) {
        return generateReport(startDate, endDate, periodType, branchId, ReportFormat.EXCEL);
    }

    @Override
    public byte[] generateCsvReport(LocalDate startDate, LocalDate endDate, String periodType, Long branchId) {
        return generateReport(startDate, endDate, periodType, branchId, ReportFormat.CSV);
    }

    private byte[] generateReport(LocalDate startDate, LocalDate endDate, String periodType,
                                  Long branchId, ReportFormat format) {
        try (Connection connection = dataSource.getConnection()) {

            // Adjust dates based on period type
            LocalDate adjustedStartDate = adjustStartDateForPeriod(startDate, periodType);
            LocalDate adjustedEndDate = adjustEndDateForPeriod(endDate, periodType);

            log.info("Generating {} report from {} to {}, period: {}, branch: {}",
                    format, adjustedStartDate, adjustedEndDate, periodType, branchId);

            JasperPrint jasperPrint = createJasperPrint(adjustedStartDate, adjustedEndDate, periodType, branchId, connection);

            switch (format) {
                case PDF:
                    return JasperExportManager.exportReportToPdf(jasperPrint);
                case EXCEL:
                    return exportToExcel(jasperPrint);
                case CSV:
                    return exportToCsv(jasperPrint);
                default:
                    throw new IllegalArgumentException("Unsupported report format: " + format);
            }

        } catch (Exception e) {
            log.error("Error generating {} report", format, e);
            throw new ReportGenerationException("Failed to generate " + format + " report", e);
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

    private JasperPrint createJasperPrint(LocalDate startDate, LocalDate endDate,
                                          String periodType, Long branchId, Connection connection) throws Exception {
        JasperReport jasperReport = reportCompiler.getCompiledReport(BRANCH_INCOME_REPORT);
        Map<String, Object> parameters = createReportParameters(startDate, endDate, periodType, branchId);

        log.info("Creating JasperPrint with parameters: {}", parameters);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        if (jasperPrint.getPages().isEmpty()) {
            log.warn("⚠️ Generated report has no data pages - empty result set");
        } else {
            log.info("✅ Report generated successfully with {} pages", jasperPrint.getPages().size());
        }

        return jasperPrint;
    }

    private Map<String, Object> createReportParameters(LocalDate startDate, LocalDate endDate,
                                                       String periodType, Long branchId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "Branch Income Summary Report");
        parameters.put("startDate", java.sql.Date.valueOf(startDate));
        parameters.put("endDate", java.sql.Date.valueOf(endDate));
        parameters.put("periodType", periodType != null ? periodType : "MONTHLY");
        parameters.put("branchId", branchId);
        parameters.put("generatedBy", "Property Management System");
        parameters.put("generatedAt", java.time.LocalDateTime.now().toString());
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