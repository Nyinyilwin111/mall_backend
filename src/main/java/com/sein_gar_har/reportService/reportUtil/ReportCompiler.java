package com.sein_gar_har.reportService.reportUtil;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReportCompiler {

    private Map<String, net.sf.jasperreports.engine.JasperReport> compiledReports = new HashMap<>();

    public net.sf.jasperreports.engine.JasperReport getCompiledReport(String reportName) {
        if (!compiledReports.containsKey(reportName)) {
            compileReportOnDemand(reportName);
        }
        return compiledReports.get(reportName);
    }

    private void compileReportOnDemand(String reportName) {
        InputStream reportStream = null;
        try {
            String reportPath = getReportPath(reportName);
            log.info("🔄 Compiling report: {} from path: {}", reportName, reportPath);

            reportStream = new ClassPathResource(reportPath).getInputStream();

            // Log first few lines of XML for debugging
            if (log.isDebugEnabled()) {
                String content = new String(reportStream.readAllBytes());
                String[] lines = content.split("\n");
                log.debug("📄 Report XML (first 10 lines):");
                for (int i = 0; i < Math.min(10, lines.length); i++) {
                    log.debug("  {}: {}", i + 1, lines[i].trim());
                }
                // Reset stream
                reportStream = new ClassPathResource(reportPath).getInputStream();
            }

            net.sf.jasperreports.engine.JasperReport compiledReport = JasperCompileManager.compileReport(reportStream);
            compiledReports.put(reportName, compiledReport);

            log.info("✅ Successfully compiled report: {}", reportName);
        } catch (Exception e) {
            log.error("❌ Failed to compile report: {}", reportName, e);
            throw new RuntimeException("Failed to compile report: " + reportName, e);
        } finally {
            if (reportStream != null) {
                try {
                    reportStream.close();
                } catch (Exception e) {
                    log.warn("Failed to close report stream for: {}", reportName);
                }
            }
        }
    }

    private String getReportPath(String reportName) {
        switch (reportName) {
            case "amenities_subreport":
                return "reports/spaces/amenities_subreport.jrxml";
            case "space_detail_page1":
                return "reports/spaces/space_detail_page1.jrxml";
            case "space_contact_page2":
                return "reports/spaces/space_contact_page2.jrxml";
            case "space_detail_report":
                return "reports/spaces/space_detail_report.jrxml";
            case "space_list_report":
                return "reports/spaces/space_list_report.jrxml";
            case "space_availability_report":
                return "reports/spaces/space_availability_report.jrxml";
            case "space_floor_report":
                return "reports/spaces/space_floor_report.jrxml";
            case "branch_income_report":
                return "reports/branches/BranchIncomeReport.jrxml";
            default:
                throw new IllegalArgumentException("Unknown report: " + reportName);
        }
    }

    public Map<String, net.sf.jasperreports.engine.JasperReport> getCompiledReports() {
        return new HashMap<>(compiledReports);
    }

    public void clearReportCache(String reportName) {
        if (compiledReports.remove(reportName) != null) {
            log.info("🗑️ Cleared cache for report: {}", reportName);
        } else {
            log.warn("⚠️ Report {} not found in cache", reportName);
        }
    }

    public void clearAllCache() {
        int count = compiledReports.size();
        compiledReports.clear();
        log.info("🗑️ Cleared all report caches ({} reports)", count);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportCompiler.class);
}