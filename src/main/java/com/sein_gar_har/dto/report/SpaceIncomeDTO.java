// SpaceIncomeDTO.java
package com.sein_gar_har.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceIncomeDTO {
    private String spaceCode;
    private String spaceType;
    private String floorLevel;
    private String branchName;
    private BigDecimal totalAmount;
    private LocalDate paymentDate;
    private String paymentType; // LEASE, UTILITY, OTHER
    private String tenantName;
    private String status;
    private Long paymentId; // Added for tracking
}