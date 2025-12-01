// AuditReportService.java
package com.sein_gar_har.reportService;

import com.sein_gar_har.RepositoryAudit.AuditLogRepository;
import com.sein_gar_har.auditEntity.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditReportService {

    private final AuditLogRepository auditLogRepository;

    public byte[] generateUserActivityReport(String username, LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        try {
            List<AuditLog> auditLogs = auditLogRepository.findUserActivityReport(username, startDate, endDate);

            // Load JasperReport template
            InputStream reportStream = new ClassPathResource("/jasper/templates/user_activity_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(auditLogs);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "User Activity Report - " + username);
            parameters.put("DATE_RANGE", startDate + " to " + endDate);
            parameters.put("USERNAME", username);
            parameters.put("TOTAL_ACTIONS", auditLogs.size());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException e) {
            log.error("Error loading JasperReport template", e);
            throw new JRException("Failed to load report template", e);
        }
    }

    public byte[] generateLoginActivityReport(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        try {
            List<AuditLog> loginActivities = auditLogRepository.findLoginActivityReport(startDate, endDate);

            // Load JasperReport template
            InputStream reportStream = new ClassPathResource("/jasper/templates/login_activity_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(loginActivities);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "User Login Activity Report");
            parameters.put("DATE_RANGE", startDate + " to " + endDate);
            parameters.put("TOTAL_LOGINS", loginActivities.size());

            long successfulLogins = loginActivities.stream()
                    .filter(log -> "SUCCESS".equals(getLoginStatus(log)))
                    .count();
            parameters.put("SUCCESSFUL_LOGINS", successfulLogins);
            parameters.put("FAILED_LOGINS", loginActivities.size() - successfulLogins);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException e) {
            log.error("Error loading JasperReport template", e);
            throw new JRException("Failed to load report template", e);
        }
    }

    public byte[] generateRoleChangeAuditReport(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        try {
            List<AuditLog> roleChanges = auditLogRepository.findRoleChangeAudit(startDate, endDate);

            // Load JasperReport template
            InputStream reportStream = new ClassPathResource("/jasper/templates/role_change_audit.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(roleChanges);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "User Role Change Audit Report");
            parameters.put("DATE_RANGE", startDate + " to " + endDate);
            parameters.put("TOTAL_ROLE_CHANGES", roleChanges.size());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException e) {
            log.error("Error loading JasperReport template", e);
            throw new JRException("Failed to load report template", e);
        }
    }

    public byte[] generateComprehensiveAuditReport(LocalDateTime startDate, LocalDateTime endDate) throws JRException {
        try {
            List<AuditLog> allActivities = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

            // Load JasperReport template
            InputStream reportStream = new ClassPathResource("/jasper/templates/comprehensive_audit_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(allActivities);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Comprehensive Audit Report");
            parameters.put("DATE_RANGE", startDate + " to " + endDate);
            parameters.put("TOTAL_ACTIVITIES", allActivities.size());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException e) {
            log.error("Error loading JasperReport template", e);
            throw new JRException("Failed to load report template", e);
        }
    }

    // Helper method to extract login status from AuditLog
    private String getLoginStatus(AuditLog auditLog) {
        if (!"LOGIN".equalsIgnoreCase(auditLog.getAction())) {
            return "N/A";
        }

        String newValues = auditLog.getNewValues();
        if (newValues != null) {
            if (newValues.contains("\"status\":\"SUCCESS\"")) {
                return "SUCCESS";
            } else if (newValues.contains("\"status\":\"FAILED\"")) {
                return "FAILED";
            }
        }
        return "UNKNOWN";
    }
}