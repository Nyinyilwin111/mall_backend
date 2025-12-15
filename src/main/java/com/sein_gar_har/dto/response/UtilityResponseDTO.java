package com.sein_gar_har.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UtilityResponseDTO {
    private Long utilityId;
    private UUID spaceId;
    private String spaceCode;
    private String spaceLocation;

<<<<<<< HEAD
=======
    // ✅ UPDATED: Include tenant information
>>>>>>> 7849d7fed339778c291f082b6b5ba33d53d04c80
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;

    private String utilityType;
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String billingPeriod;
    private String usageUnit;
    private Double previousReading;
    private Double currentReading;
    private Double usageAmount;
    private String recordStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}