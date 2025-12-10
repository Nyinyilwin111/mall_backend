//package com.sein_gar_har.dto.request;
//
//import lombok.Getter;
//import lombok.Setter;
//import jakarta.validation.constraints.NotNull;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.UUID;
//
//@Getter
//@Setter
//public class UtilityRequestDTO {
//
//    @NotNull(message = "Space ID is required")
//    private UUID spaceId;
//
//    @NotNull(message = "Utility type is required")
//    private String utilityType;
//
//    private String description;
//
//    @NotNull(message = "Due date is required")
//    private LocalDate dueDate;
//
//    private String billingPeriod;
//    private String usageUnit;
//    private Double previousReading;
//    private Double currentReading;
//
//    // ADDED: Amount field
//    private BigDecimal amount;
//}
// UtilityRequestDTO.java - UPDATED
package com.sein_gar_har.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UtilityRequestDTO {

    @NotNull(message = "Space ID is required")
    private UUID spaceId;

    // ✅ ADDED: Tenant ID field (will be auto-filled by backend)
    private UUID tenantId;

    @NotNull(message = "Utility type is required")
    private String utilityType;

    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String billingPeriod;
    private String usageUnit;
    private Double previousReading;
    private Double currentReading;
    private BigDecimal amount;
}