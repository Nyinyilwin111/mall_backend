package com.sein_gar_har.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchIncomeDTO {
    private Long branchId;
    private String branchName;
    private Long paymentId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentType;
    private Long leaseId;
    private String tenantName;
    private String spaceName;
    private String utilityType;
}