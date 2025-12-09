//// JasperReportService.java (Updated)
//package com.sein_gar_har.reportService;
//
//import com.sein_gar_har.dto.report.ReportParamsDTO;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jasperreports.engine.*;
//import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
//import net.sf.jasperreports.export.*;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SpaceIncomeService {
//
//    private final DataSource dataSource;
//    private static final String REPORT_TEMPLATE = "reports/income_report.jrxml";
//
//    public void generateIncomeReport(ReportParamsDTO params, HttpServletResponse response) throws Exception {
//        Connection connection = null;
//        try {
//            connection = dataSource.getConnection();
//
//            // Load single JRXML file
//            InputStream reportStream = new ClassPathResource(REPORT_TEMPLATE).getInputStream();
//
//            // Compile report
//            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
//
//            // Set parameters
//            Map<String, Object> parameters = buildParameters(params);
//
//            // Fill report with data
//            JasperPrint jasperPrint = JasperFillManager.fillReport(
//                    jasperReport, parameters, connection);
//
//            // Export based on format
//            exportReport(jasperPrint, params.getFormat(), response);
//
//            log.info("Report generated successfully for branch: {}, period: {}",
//                    params.getBranchId(), params.getPeriod());
//
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (Exception e) {
//                    log.error("Error closing connection", e);
//                }
//            }
//        }
//    }
//
//    private Map<String, Object> buildParameters(ReportParamsDTO params) {
//        Map<String, Object> parameters = new HashMap<>();
//
//        parameters.put("BRANCH_ID", params.getBranchId());
//        parameters.put("REPORT_DATE", params.getReportDate());
//        parameters.put("PERIOD_TYPE", params.getPeriod().toString());
//
//        // Build dynamic title based on period
//        String title = buildReportTitle(params);
//        parameters.put("REPORT_TITLE", title);
//
//        return parameters;
//    }
//
//    private String buildReportTitle(ReportParamsDTO params) {
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
//        LocalDate date = params.getReportDate();
//
//        return switch (params.getPeriod()) {
//            case DAILY -> "Daily Income Report - " + date.format(dateFormatter);
//            case MONTHLY -> "Monthly Income Report - " + date.format(monthFormatter);
//            case YEARLY -> "Yearly Income Report - " + date.getYear();
//        };
//    }
//
//    private void exportReport(JasperPrint jasperPrint,
//                              ReportParamsDTO.ReportFormat format,
//                              HttpServletResponse response) throws JRException {
//
//        response.setCharacterEncoding("UTF-8");
//
//        switch (format) {
//            case PDF:
//                exportAsPdf(jasperPrint, response);
//                break;
//            case EXCEL:
//                exportAsExcel(jasperPrint, response);
//                break;
//            case HTML:
//                exportAsHtml(jasperPrint, response);
//                break;
//        }
//    }
//
//    private void exportAsPdf(JasperPrint jasperPrint, HttpServletResponse response) throws JRException {
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "inline; filename=income_report.pdf");
//        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
//    }
//
//    private void exportAsExcel(JasperPrint jasperPrint, HttpServletResponse response) throws JRException {
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition", "attachment; filename=income_report.xlsx");
//
//        JRXlsxExporter exporter = new JRXlsxExporter();
//        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
//
//        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
//        configuration.setOnePagePerSheet(false);
//        configuration.setRemoveEmptySpaceBetweenRows(true);
//        configuration.setDetectCellType(true);
//        configuration.setWhitePageBackground(false);
//        configuration.setIgnoreGraphics(false);
//
//        exporter.setConfiguration(configuration);
//        exporter.exportReport();
//    }
//
//    private void exportAsHtml(JasperPrint jasperPrint, HttpServletResponse response) throws JRException {
//        response.setContentType("text/html");
//        response.setHeader("Content-Disposition", "inline; filename=income_report.html");
//        JasperExportManager.exportReportToHtmlFile(jasperPrint, response.getOutputStream());
//    }
//
//    public JasperPrint generateReportPreview(ReportParamsDTO params) throws Exception {
//        Connection connection = null;
//        try {
//            connection = dataSource.getConnection();
//            InputStream reportStream = new ClassPathResource(REPORT_TEMPLATE).getInputStream();
//
//            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
//            Map<String, Object> parameters = buildParameters(params);
//
//            return JasperFillManager.fillReport(jasperReport, parameters, connection);
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (Exception e) {
//                    log.error("Error closing connection", e);
//                }
//            }
//        }
//    }
//}