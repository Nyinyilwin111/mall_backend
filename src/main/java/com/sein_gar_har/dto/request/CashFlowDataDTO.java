package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowDataDTO {
    private List<String> labels;
    private List<Double> values;
    private Integer branchId;
}
