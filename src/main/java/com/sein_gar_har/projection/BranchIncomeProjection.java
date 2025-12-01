package com.sein_gar_har.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BranchIncomeProjection {
    Long getBranchId();
    String getBranchName();
    Long getPaymentId();
    BigDecimal getAmount();
    LocalDate getPaymentDate();
    String getPaymentType();
    Long getLeaseId();
    String getTenantName();
    String getSpaceName();
    String getUtilityType();
}