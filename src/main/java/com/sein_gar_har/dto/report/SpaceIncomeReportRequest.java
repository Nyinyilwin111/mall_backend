// SpaceIncomeReportRequest.java
package com.sein_gar_har.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SpaceIncomeReportRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Period type is required")
    private String periodType;

    private String spaceCode;
    private Long branchId;

    private String reportFormat = "PDF";

    // Validation methods
    public boolean isDateRangeValid() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    public boolean isDateRangeWithinLimit() {
        if (startDate == null || endDate == null) return false;
        LocalDate maxEndDate = startDate.plusYears(2);
        return !endDate.isAfter(maxEndDate);
    }
}