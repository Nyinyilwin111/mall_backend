package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDataDTO {
    private Double unpaid;
    private Double overdue;
    private Double notDue;
    private Double paid;
    private Double notDeposited;
    private Double deposited;
    private Last365Days last365Days;
    private Integer branchId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Last365Days {
        private Integer total;
        private Integer overdue;
    }
}