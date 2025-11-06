package com.spring.DTO.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class LeaseRequest {
    private UUID tenantId;  // CHANGE FROM Long to UUID
    private Long spaceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;

    // file upload fields
    private MultipartFile contractFile;
}