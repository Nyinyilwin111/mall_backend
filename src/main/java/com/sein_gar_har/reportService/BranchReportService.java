package com.sein_gar_har.reportService;

public interface BranchReportService {
    byte[] generateBranchListReport(String format);

    byte[] generateBranchDetailReport(Long branchId, String format);

    byte[] generateBranchUsersReport(Long branchId, String format);

    byte[] generateBranchAnalyticsReport(String format);
}
