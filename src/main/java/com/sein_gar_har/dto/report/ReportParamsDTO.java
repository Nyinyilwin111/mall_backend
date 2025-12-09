package com.sein_gar_har.dto.report;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportParamsDTO {
    private Long branchId;
    private LocalDate reportDate;
    private ReportPeriod period;
    private ReportFormat format;

    public enum ReportPeriod {
        DAILY, MONTHLY, YEARLY
    }

    public enum ReportFormat {
        PDF, EXCEL, HTML
    }
}