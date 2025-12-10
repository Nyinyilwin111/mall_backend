package com.sein_gar_har.reportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditReportService {

    // Use the audit data source for JasperReports
    @Qualifier("auditDataSource")
    private final DataSource auditDataSource;

    @Qualifier("auditJdbcTemplate")
    private final JdbcTemplate auditJdbcTemplate;

    private final Map<String, JasperReport> compiledReports = new ConcurrentHashMap<>();

    private JasperReport getCompiledReport(String templateName) throws JRException, IOException {
        return compiledReports.computeIfAbsent(templateName, key -> {
            try {
                String templatePath = "/reports/auditlog/" + templateName + ".jrxml";
                InputStream reportStream = new ClassPathResource(templatePath).getInputStream();
                log.info("Compiling JasperReport template: {}", templatePath);
                return JasperCompileManager.compileReport(reportStream);
            } catch (IOException | JRException e) {
                log.error("Failed to compile report template: {}", templateName, e);
                throw new RuntimeException("Failed to compile report template: " + templateName, e);
            }
        });
    }

    private byte[] generateReport(String templateName, Map<String, Object> parameters, boolean isExcel) throws JRException {
        Connection connection = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Use audit database connection
            connection = auditDataSource.getConnection();
            log.debug("Connected to audit database for report generation: {}", connection.getMetaData().getURL());

            JasperReport jasperReport = getCompiledReport(templateName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            if (isExcel) {
                JRXlsExporter exporter = new JRXlsExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                SimpleOutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(baos);
                exporter.setExporterOutput(output);

                SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
                configuration.setOnePagePerSheet(false);
                configuration.setRemoveEmptySpaceBetweenRows(true);
                configuration.setDetectCellType(true);
                configuration.setWhitePageBackground(false);

                exporter.setConfiguration(configuration);
                exporter.exportReport();
            } else {
                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                SimpleOutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(baos);
                exporter.setExporterOutput(output);

                SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
                exporter.setConfiguration(configuration);
                exporter.exportReport();
            }

            return baos.toByteArray();
        } catch (IOException | SQLException e) {
            log.error("Error generating report from template: {}", templateName, e);
            throw new JRException("Failed to generate report", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
            try {
                baos.close();
            } catch (IOException e) {
                log.error("Error closing output stream", e);
            }
        }
    }

    // Update all methods to use auditJdbcTemplate
    public byte[] generateUserActivityReport(String username, LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        // Use auditJdbcTemplate
        Integer totalActions = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE performedBy = ? AND timestamp BETWEEN ? AND ?",
                Integer.class,
                username,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        log.debug("Total actions for user {}: {}", username, totalActions);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "User Activity Report - " + username);
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("USERNAME", username);
        parameters.put("TOTAL_ACTIONS", totalActions != null ? totalActions : 0);
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("user_activity_report", parameters, false);
    }

    public byte[] generateUserActivityReportExcel(String username, LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalActions = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE performedBy = ? AND timestamp BETWEEN ? AND ?",
                Integer.class,
                username,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "User Activity Report - " + username);
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("USERNAME", username);
        parameters.put("TOTAL_ACTIONS", totalActions != null ? totalActions : 0);
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("user_activity_report", parameters, true);
    }

    public byte[] generateLoginActivityReport(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalLogins = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE action = 'LOGIN' AND timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        Integer successfulLogins = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE action = 'LOGIN' AND newValues LIKE '%\"status\":\"SUCCESS\"%' AND timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "User Login Activity Report");
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("TOTAL_LOGINS", totalLogins != null ? totalLogins : 0);
        parameters.put("SUCCESSFUL_LOGINS", successfulLogins != null ? successfulLogins : 0);
        parameters.put("FAILED_LOGINS", (totalLogins != null ? totalLogins : 0) - (successfulLogins != null ? successfulLogins : 0));
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("login_activity_report", parameters, false);
    }

    public byte[] generateLoginActivityReportExcel(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalLogins = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE action = 'LOGIN' AND timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        Integer successfulLogins = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE action = 'LOGIN' AND newValues LIKE '%\"status\":\"SUCCESS\"%' AND timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "User Login Activity Report");
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("TOTAL_LOGINS", totalLogins != null ? totalLogins : 0);
        parameters.put("SUCCESSFUL_LOGINS", successfulLogins != null ? successfulLogins : 0);
        parameters.put("FAILED_LOGINS", (totalLogins != null ? totalLogins : 0) - (successfulLogins != null ? successfulLogins : 0));
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("login_activity_report", parameters, true);
    }

    public byte[] generateRoleChangeAuditReport(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalRoleChanges = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE action = 'UPDATE' AND tableAffected = 'User' AND (newValues LIKE '%roles%' OR userFriendlyMessage LIKE '%role%') AND timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "User Role Change Audit Report");
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("TOTAL_ROLE_CHANGES", totalRoleChanges != null ? totalRoleChanges : 0);
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("role_change_audit", parameters, false);
    }

    public byte[] generateRoleChangeAuditReportExcel(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalRoleChanges = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE action = 'UPDATE' AND tableAffected = 'User' AND (newValues LIKE '%roles%' OR userFriendlyMessage LIKE '%role%') AND timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "User Role Change Audit Report");
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("TOTAL_ROLE_CHANGES", totalRoleChanges != null ? totalRoleChanges : 0);
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("role_change_audit", parameters, true);
    }

    public byte[] generateComprehensiveAuditReport(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalActivities = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Comprehensive Audit Report");
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("TOTAL_ACTIVITIES", totalActivities != null ? totalActivities : 0);
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("comprehensive_audit_report", parameters, false);
    }

    public byte[] generateComprehensiveAuditReportExcel(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        Integer totalActivities = auditJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditlog WHERE timestamp BETWEEN ? AND ?",
                Integer.class,
                java.sql.Timestamp.valueOf(startDate),
                java.sql.Timestamp.valueOf(endDate)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Comprehensive Audit Report");
        parameters.put("START_DATE", java.sql.Timestamp.valueOf(startDate));
        parameters.put("END_DATE", java.sql.Timestamp.valueOf(endDate));
        parameters.put("TOTAL_ACTIVITIES", totalActivities != null ? totalActivities : 0);
        parameters.put("GENERATED_ON", LocalDateTime.now().format(formatter));

        return generateReport("comprehensive_audit_report", parameters, true);
    }
}