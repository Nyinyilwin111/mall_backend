package com.sein_gar_har.reportService.ReportServiceImpl;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.reportService.BranchReportService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BranchReportServiceImpl implements BranchReportService {

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;

    // ✅ FIX: Updated path to match your actual directory structure
    private static final String REPORT_BASE_PATH = "reports/branches/";

    @Override
    public byte[] generateBranchListReport(String format) {
        try {
            log.info("Generating branch list report in format: {}", format);

            // Fetch all branches
            List<Branch> branches = branchRepository.findAll();

            // ✅ Add null check
            if (branches == null || branches.isEmpty()) {
                log.warn("No branches found for report");
                return new byte[0];
            }

            log.info("Found {} branches for report", branches.size());

            // Prepare data for report - match the fields in your JRXML
            List<Map<String, Object>> reportData = branches.stream()
                    .map(this::convertBranchToMap)
                    .collect(Collectors.toList());

            // Load JasperReport template - use EXACT file name
            JasperReport jasperReport = loadAndCompileReport("branch_list.jrxml");

            // Parameters for the report - match your JRXML parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Branch List Report");
            parameters.put("TOTAL_BRANCHES", branches.size());
            parameters.put("GENERATED_DATE", new Date());
            parameters.put("LOGO_IMAGE", "classpath:image/SGH-logo.png"); // Set to null if no logo

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            return exportReport(jasperPrint, format, "branch-list-report");

        } catch (Exception e) {
            log.error("Error generating branch list report", e);
            throw new RuntimeException("Failed to generate branch list report: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateBranchDetailReport(Long branchId, String format) {
        try {
            log.info("Generating branch detail report for branch ID: {} in format: {}", branchId, format);

            // Fetch branch
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + branchId));

            // Prepare data for report
            Map<String, Object> branchData = convertBranchToDetailedMap(branch);

            // Load JasperReport template
            JasperReport jasperReport = loadAndCompileReport("branch_detail.jrxml");

            // Parameters for the report - match your JRXML parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Branch Detail Report");
            parameters.put("BRANCH_NAME", branch.getName());
            parameters.put("BRANCH_ADDRESS", branch.getAddress());
            parameters.put("BRANCH_PHONE", branch.getPhoneNumber() != null ? branch.getPhoneNumber() : "N/A");
            parameters.put("TOTAL_USERS", branch.getUsers() != null ? branch.getUsers().size() : 0);
            parameters.put("GENERATED_DATE", new Date());

            // For subreports - users associated with this branch
            if (branch.getUsers() != null && !branch.getUsers().isEmpty()) {
                List<Map<String, Object>> usersData = branch.getUsers().stream()
                        .map(this::convertUserToMap)
                        .collect(Collectors.toList());
                parameters.put("USERS_DATA_SOURCE", new JRBeanCollectionDataSource(usersData));
            } else {
                parameters.put("USERS_DATA_SOURCE", new JRBeanCollectionDataSource(Collections.emptyList()));
            }

            // Create data source with single branch
            List<Map<String, Object>> dataList = Collections.singletonList(branchData);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            return exportReport(jasperPrint, format, "branch-detail-" + branchId);

        } catch (Exception e) {
            log.error("Error generating branch detail report for branch ID: {}", branchId, e);
            throw new RuntimeException("Failed to generate branch detail report: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateBranchUsersReport(Long branchId, String format) {
        try {
            log.info("Generating branch users report for branch ID: {} in format: {}", branchId, format);

            // Fetch branch with users
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + branchId));

            // Get users associated with this branch
            Set<User> users = branch.getUsers();
            log.info("Found {} users for branch: {}", users != null ? users.size() : 0, branch.getName());

            // ✅ Handle case when no users found
            if (users == null || users.isEmpty()) {
                log.warn("No users found for branch ID: {}", branchId);
                // You can either return empty report or throw specific exception
                // return new byte[0]; // Return empty
                throw new RuntimeException("No users found for branch: " + branch.getName());
            }

            // Prepare user data for report
            List<Map<String, Object>> usersData = users.stream()
                    .map(this::convertUserToMap)
                    .collect(Collectors.toList());

            // Load JasperReport template for users
            JasperReport jasperReport = loadAndCompileReport("branch_users.jrxml");

            // Parameters for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Branch Users Report - " + branch.getName());
            parameters.put("COMPANY_NAME", "Sein Gar Har Real Estate");
            parameters.put("REPORT_DATE", new Date());
            parameters.put("GENERATED_BY", "System Administrator");
            parameters.put("REPORT_ID", "BRANCH-USERS-2025");
            parameters.put("GENERATED_DATE", new Date());
            parameters.put("COMPANY_LOGO", null);

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(usersData);

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            return exportReport(jasperPrint, format, "branch-users-" + branchId);

        } catch (Exception e) {
            log.error("Error generating branch users report for branch ID: {}", branchId, e);
            throw new RuntimeException("Failed to generate branch users report: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateBranchAnalyticsReport(String format) {
        try {
            log.info("Generating branch analytics report in format: {}", format);

            // Fetch all branches with statistics
            List<Branch> branches = branchRepository.findAll();

            // Calculate analytics
            long totalBranches = branches.size();
            long totalUsers = branches.stream()
                    .mapToLong(branch -> branch.getUsers() != null ? branch.getUsers().size() : 0)
                    .sum();
            double averageUsersPerBranch = totalBranches > 0 ? (double) totalUsers / totalBranches : 0;

            // Prepare analytics data
            List<Map<String, Object>> analyticsData = branches.stream()
                    .map(branch -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("branchName", branch.getName());
                        data.put("userCount", branch.getUsers() != null ? branch.getUsers().size() : 0);
                        data.put("address", branch.getAddress());
                        data.put("phone", branch.getPhoneNumber() != null ? branch.getPhoneNumber() : "N/A");
                        data.put("createdDate", branch.getCreatedAt()); // This is LocalDateTime
                        return data;
                    })
                    .collect(Collectors.toList());

            // Load JasperReport template
            JasperReport jasperReport = loadAndCompileReport("branch_analytics.jrxml");

            // Parameters for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Branch Analytics Report");
            parameters.put("COMPANY_NAME", "Sein Gar Har Real Estate");
            parameters.put("REPORT_DATE", new Date());
            parameters.put("GENERATED_BY", "System Administrator");
            parameters.put("REPORT_ID", "BRANCH-ANALYTICS-2025");
            parameters.put("TOTAL_BRANCHES", totalBranches);
            parameters.put("TOTAL_USERS", totalUsers);
            parameters.put("AVERAGE_USERS_PER_BRANCH", String.format("%.2f", averageUsersPerBranch));
            parameters.put("GENERATED_DATE", new Date());
            parameters.put("REPORT_PERIOD", "All Time");
            parameters.put("COMPANY_LOGO", null);

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(analyticsData);

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            return exportReport(jasperPrint, format, "branch-analytics-report");

        } catch (Exception e) {
            log.error("Error generating branch analytics report", e);
            throw new RuntimeException("Failed to generate branch analytics report: " + e.getMessage(), e);
        }
    }

    // ✅ IMPROVED: Enhanced report loading with multiple fallback options and better error handling
    private JasperReport loadAndCompileReport(String reportFileName) {
        try {
            log.info("🔍 Attempting to load JasperReport template: {}", reportFileName);

            // Try multiple possible locations
            String[] possiblePaths = {
                    "reports/branches/" + reportFileName,
                    "branches/" + reportFileName,
                    reportFileName
            };

            InputStream reportStream = null;
            String foundPath = null;

            for (String path : possiblePaths) {
                try {
                    reportStream = getClass().getClassLoader().getResourceAsStream(path);
                    if (reportStream != null) {
                        foundPath = path;
                        log.info("✅ Found template at: {}", foundPath);
                        break;
                    }
                } catch (Exception e) {
                    log.debug("Template not found at: {}", path);
                }
            }

            // If still not found, try ClassPathResource as fallback
            if (reportStream == null) {
                try {
                    ClassPathResource resource = new ClassPathResource("reports/branches/" + reportFileName);
                    if (resource.exists()) {
                        reportStream = resource.getInputStream();
                        foundPath = "reports/branches/" + reportFileName;
                        log.info("✅ Found template using ClassPathResource at: {}", foundPath);
                    }
                } catch (Exception e) {
                    log.debug("ClassPathResource also failed to find template");
                }
            }

            // Last resort: try absolute path from file system
            if (reportStream == null) {
                try {
                    java.io.File file = new java.io.File("src/main/resources/reports/branches/" + reportFileName);
                    if (file.exists()) {
                        reportStream = new java.io.FileInputStream(file);
                        foundPath = file.getAbsolutePath();
                        log.info("✅ Found template using filesystem at: {}", foundPath);
                    }
                } catch (Exception e) {
                    log.debug("Filesystem lookup also failed");
                }
            }

            if (reportStream == null) {
                // Debug: List what's available in the classpath
                debugClasspathResources();
                throw new FileNotFoundException("Report template not found. Tried: " +
                        Arrays.toString(possiblePaths) + ". Please ensure the file exists in src/main/resources/reports/branches/");
            }

            // Compile the report
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            reportStream.close();

            log.info("✅ Successfully compiled report: {}", reportFileName);
            return jasperReport;

        } catch (Exception e) {
            log.error("❌ Failed to load JasperReport template: {}", reportFileName, e);
            throw new RuntimeException("JasperReport template not found or invalid: " + reportFileName +
                    ". Please ensure the file exists in src/main/resources/reports/branches/", e);
        }
    }

    // ✅ NEW: Debug method to help identify resource location issues
    private void debugClasspathResources() {
        try {
            log.info("🔍 Debugging classpath resources...");

            // Check reports directory
            java.net.URL reportsUrl = getClass().getClassLoader().getResource("reports/");
            log.info("Reports directory URL: {}", reportsUrl);

            if (reportsUrl != null) {
                try {
                    java.io.File reportsDir = new java.io.File(reportsUrl.toURI());
                    if (reportsDir.exists() && reportsDir.isDirectory()) {
                        String[] files = reportsDir.list();
                        log.info("Files in reports directory: {}", Arrays.toString(files));

                        // Check branches subdirectory
                        java.io.File branchesDir = new java.io.File(reportsDir, "branches");
                        if (branchesDir.exists() && branchesDir.isDirectory()) {
                            String[] branchFiles = branchesDir.list();
                            log.info("Files in reports/branches directory: {}", Arrays.toString(branchFiles));
                        } else {
                            log.warn("branches subdirectory does not exist");
                        }
                    }
                } catch (Exception e) {
                    log.debug("Cannot list files from URL: {}", e.getMessage());
                }
            }

            // Try to list all resources
            try {
                java.util.Enumeration<java.net.URL> resources = getClass().getClassLoader().getResources("reports/");
                while (resources.hasMoreElements()) {
                    java.net.URL resource = resources.nextElement();
                    log.info("Found resource: {}", resource);
                }
            } catch (Exception e) {
                log.debug("Cannot enumerate resources: {}", e.getMessage());
            }

            // Check target directory
            try {
                java.io.File targetDir = new java.io.File("target/classes/reports/branches/");
                if (targetDir.exists()) {
                    String[] targetFiles = targetDir.list();
                    log.info("Files in target/classes/reports/branches/: {}", Arrays.toString(targetFiles));
                } else {
                    log.warn("target/classes/reports/branches/ directory does not exist");
                }
            } catch (Exception e) {
                log.debug("Cannot check target directory: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.debug("Debug error: {}", e.getMessage());
        }
    }

    // ✅ NEW: Initialize and test report templates on startup
    @javax.annotation.PostConstruct
    public void init() {
        try {
            log.info("🔍 Initializing BranchReportService - checking report templates...");

            // Test if our analytics report exists
            testReportExistence("branch_analytics.jrxml");
            testReportExistence("branch_list.jrxml");
            testReportExistence("branch_detail.jrxml");
            testReportExistence("branch_users.jrxml");

            log.info("✅ BranchReportService initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize BranchReportService", e);
        }
    }

    // ✅ NEW: Test method to verify report template existence
    private void testReportExistence(String reportFileName) {
        try {
            JasperReport report = loadAndCompileReport(reportFileName);
            log.info("✅ Report template '{}' is available and compiles successfully", reportFileName);
        } catch (Exception e) {
            log.error("❌ Report template '{}' is NOT available: {}", reportFileName, e.getMessage());
        }
    }

    // Helper methods for data conversion - MUST match your JRXML field names
    private Map<String, Object> convertBranchToMap(Branch branch) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", branch.getId());
        data.put("name", branch.getName());
        data.put("address", branch.getAddress());
        data.put("phoneNumber", branch.getPhoneNumber() != null ? branch.getPhoneNumber() : "N/A");
        data.put("createdAt", branch.getCreatedAt());
        data.put("updatedAt", branch.getUpdatedAt());
        data.put("userCount", branch.getUsers() != null ? branch.getUsers().size() : 0);
        return data;
    }

    private Map<String, Object> convertBranchToDetailedMap(Branch branch) {
        Map<String, Object> data = convertBranchToMap(branch);
        // Add additional fields for detail report if needed
        data.put("description", "Branch located at " + branch.getAddress());
        return data;
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> data = new HashMap<>();

        // ✅ Handle UUID id properly - convert to String
        data.put("id", user.getId() != null ? user.getId().toString() : "N/A");

        // Add all fields that the subreport expects
        data.put("username", user.getEmail()); // Using email as username since username field doesn't exist
        data.put("email", user.getEmail() != null ? user.getEmail() : "N/A");
        data.put("firstName", extractFirstName(user.getFullName()));
        data.put("lastName", extractLastName(user.getFullName()));
        data.put("fullName", user.getFullName() != null ? user.getFullName() : "N/A");

        // ✅ Fix roles conversion - handle null safely and format properly
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            String rolesString = user.getRoles().stream()
                    .map(role -> role.getName()) // Assuming Role entity has getName() method
                    .collect(Collectors.joining(", "));
            data.put("role", rolesString);
        } else {
            data.put("role", "No Role");
        }

        data.put("status", user.isEnabled() ? "Active" : "Inactive");
        return data;
    }

    // Helper methods to extract first and last name from fullName
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "N/A";
        }
        String[] names = fullName.trim().split("\\s+");
        return names[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "N/A";
        }
        String[] names = fullName.trim().split("\\s+");
        return names.length > 1 ? names[names.length - 1] : "";
    }

    // Export report in different formats
    private byte[] exportReport(JasperPrint jasperPrint, String format, String reportName) throws JRException {
        log.info("Exporting report '{}' in format: {}", reportName, format);

        switch (format.toLowerCase()) {
            case "pdf":
                byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                log.info("PDF report generated, size: {} bytes", pdfBytes.length);
                return pdfBytes;

            case "excel":
            case "xls":
                byte[] xlsBytes = exportToExcel(jasperPrint, false);
                log.info("Excel (XLS) report generated, size: {} bytes", xlsBytes.length);
                return xlsBytes;

            case "xlsx":
                byte[] xlsxBytes = exportToExcel(jasperPrint, true);
                log.info("Excel (XLSX) report generated, size: {} bytes", xlsxBytes.length);
                return xlsxBytes;

            default:
                log.warn("Unknown format '{}', defaulting to PDF", format);
                return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    private byte[] exportToExcel(JasperPrint jasperPrint, boolean isXlsx) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (isXlsx) {
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
        } else {
            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(false);
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);

            exporter.setConfiguration(configuration);
            exporter.exportReport();
        }

        return outputStream.toByteArray();
    }
}