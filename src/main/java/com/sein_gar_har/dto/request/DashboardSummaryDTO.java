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
public class DashboardSummaryDTO {
    private List<DashboardMetricDTO> rentalMetrics;
    private AccountsReceivableDTO accountsReceivable;
    private CashFlowDataDTO cashFlowData;
    private InvoiceDataDTO invoiceData;
    private List<ExpenseDataDTO> expenseData;
    private XeroMetricsDTO xeroMetrics;
    private List<PaymentTargetDataDTO> paymentTargetData;
    private List<TimeSeriesDataDTO> timeSeriesData;
    private RentalMetricsDTO rentalAnalytics;
}