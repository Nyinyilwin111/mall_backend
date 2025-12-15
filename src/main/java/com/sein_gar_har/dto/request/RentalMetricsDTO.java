package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalMetricsDTO {
    private Double totalRevenue;
    private Integer occupancyRate;
    private Double averageDailyRate;
    private Integer newBookings;
    private Integer extensions;
    private Integer cancellations;
    private Double maintenanceCosts;
    private Double utilityCollections;
    private Integer branchId;
}
