package com.sein_gar_har.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BranchIncomeReportRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Period type is required")
    private String periodType; // DAILY, MONTHLY, YEARLY

    private Long branchId;

    private String reportFormat = "PDF"; // PDF, EXCEL, CSV

    // Validation methods
    public boolean isDateRangeValid() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    public boolean isStartDateValid() {
        return startDate != null && !startDate.isAfter(LocalDate.now());
    }

    public boolean isDateRangeWithinLimit() {
        if (startDate == null || endDate == null) return false;

        // Limit date range to 2 years
        LocalDate maxEndDate = startDate.plusYears(2);
        return !endDate.isAfter(maxEndDate);
    }

    // Helper method to adjust dates based on period type
    public void adjustDatesForPeriodType() {
        if (startDate == null || endDate == null || periodType == null) return;

        switch (periodType.toUpperCase()) {
            case "YEARLY":
                // Set start to first day of year, end to last day of year
                startDate = LocalDate.of(startDate.getYear(), 1, 1);
                endDate = LocalDate.of(endDate.getYear(), 12, 31);
                break;
            case "MONTHLY":
                // Set start to first day of month, end to last day of month
                startDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), 1);
                endDate = LocalDate.of(endDate.getYear(), endDate.getMonth(), endDate.lengthOfMonth());
                break;
            case "DAILY":
                // No adjustment needed for daily
                break;
        }
    }
}