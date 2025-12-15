package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.*;
import com.sein_gar_har.dto.response.BranchResponseDTO;

import java.util.List;

public interface BranchService {
    List<BranchResponseDTO> getAllBranches();
    BranchResponseDTO getBranchById(Long id);
    BranchResponseDTO createBranch(CreateBranchRequestDTO request);
    BranchResponseDTO updateBranch(Long id, UpdateBranchRequestDTO request);
    void deleteBranch(Long id);
    boolean existsByName(String name);
    List<BranchResponseDTO> getBranchesForCurrentUser();

//    for accounting

    // Rental Performance Metrics
    List<DashboardMetricDTO> getRentalMetrics(Integer branchId);

    // Accounts Receivable
    AccountsReceivableDTO getAccountsReceivable(Integer branchId);

    // Cash Flow Data
    CashFlowDataDTO getCashFlowData(Integer branchId);

    // Invoice Data
    InvoiceDataDTO getInvoiceData(Integer branchId);

    // Expense Data
    List<ExpenseDataDTO> getExpenseData(Integer branchId);

    // XERO Metrics
    XeroMetricsDTO getXeroMetrics(Integer branchId);

    // Payment Target Data
    List<PaymentTargetDataDTO> getPaymentTargetData(Integer branchId);

    // Time Series Data
    List<TimeSeriesDataDTO> getTimeSeriesData(Integer branchId);

    // Rental Analytics
    RentalMetricsDTO getRentalAnalytics(Integer branchId);

}