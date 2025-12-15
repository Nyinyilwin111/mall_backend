package com.sein_gar_har.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class LeaseRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Space ID is required")
    private UUID spaceId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    private BigDecimal rentAmount;

    private BigDecimal depositAmount;

    private String status;

    private String companyName;

    private String tenantType;

    private String tenantTrade;

    private String nrc;

    private String address;

    private String heir;

    //new column
    private String contactPhone;

    private MultipartFile contractFile;
}