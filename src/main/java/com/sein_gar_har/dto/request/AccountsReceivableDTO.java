package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsReceivableDTO {
    private Double total;
    private Double current;
    @Builder.Default private Double days1_30 = 0.0;
    @Builder.Default private Double days31_60 = 0.0;
    @Builder.Default private Double days61_90 = 0.0;
    @Builder.Default private Double over90 = 0.0;
    private Integer branchId;
}