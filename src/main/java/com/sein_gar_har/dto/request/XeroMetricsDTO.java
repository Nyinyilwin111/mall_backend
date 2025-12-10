package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XeroMetricsDTO {
    private Double accountsReceivable;
    private Double accountsPayable;
    private Double income;
    private Double bankBalance;
    private Integer branchId;
}
