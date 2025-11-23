package com.sein_gar_har.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UtilityUpdateRequestDTO {
    private String utilityType;
    private String description;
    private LocalDate dueDate;
    private String billingPeriod;
    private String usageUnit;
    private Double previousReading;
    private Double currentReading;

    // ADDED: Amount field
    private BigDecimal amount;
}