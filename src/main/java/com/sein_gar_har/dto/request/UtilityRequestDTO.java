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
<<<<<<< HEAD


=======
// UtilityRequestDTO.java - UPDATED
>>>>>>> 7849d7fed339778c291f082b6b5ba33d53d04c80
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

<<<<<<< HEAD
=======
    // ✅ ADDED: Tenant ID field (will be auto-filled by backend)
>>>>>>> 7849d7fed339778c291f082b6b5ba33d53d04c80
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