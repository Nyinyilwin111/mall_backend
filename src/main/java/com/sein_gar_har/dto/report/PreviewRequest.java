package com.sein_gar_har.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PreviewRequest {

    @NotNull(message = "Selected date is required")
    private LocalDate selectedDate;

    @NotNull(message = "Period type is required")
    private String periodType; // DAILY, MONTHLY, YEARLY

    private Long branchId;
}