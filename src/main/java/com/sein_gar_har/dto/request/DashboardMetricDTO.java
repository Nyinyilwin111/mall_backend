package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricDTO {
    private String label;
    private Double value;
    private String currency;
    private Double change;
    private String trend;
    private Integer branchId;
}