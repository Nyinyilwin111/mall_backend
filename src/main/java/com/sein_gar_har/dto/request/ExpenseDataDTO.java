package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDataDTO {
    private String period;
    private Double amount;
    private String category;
    private Integer branchId;
}
