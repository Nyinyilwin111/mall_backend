package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesDataDTO {
    private String month;
    private Double receivable;
    private Double payable;
    private Integer branchId;
}
