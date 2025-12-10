package com.sein_gar_har.controller;

import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.dto.request.*;
import com.sein_gar_har.dto.response.ApiResponse;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BranchResponseDTO>>> getAllBranches() {
        List<BranchResponseDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully", branches));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> getBranchById(@PathVariable Long id) {
        BranchResponseDTO branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Branch retrieved successfully", branch));
    }

    @GetMapping("/my-branches")
    public ResponseEntity<ApiResponse<List<BranchResponseDTO>>> getMyBranches(Principal principal) {
        List<BranchResponseDTO> branches = branchService.getBranchesForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully", branches));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> createBranch(
            @Valid @RequestBody CreateBranchRequestDTO request) {

        System.out.println("=== CONTROLLER CREATE BRANCH ===");

        BranchResponseDTO createdBranch = branchService.createBranch(request);
        return new ResponseEntity<>(
                ApiResponse.created("Branch created successfully", createdBranch),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequestDTO request) {
        BranchResponseDTO updatedBranch = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updatedBranch));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully"));
    }


//    for accounting
@GetMapping("/rental-metrics")
public ResponseEntity<List<DashboardMetricDTO>> getRentalMetrics(
        @RequestParam(required = false) Integer branchId) {
    return ResponseEntity.ok(branchService.getRentalMetrics(branchId));
}

    @GetMapping("/accounts-receivable")
    public ResponseEntity<AccountsReceivableDTO> getAccountsReceivable(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getAccountsReceivable(branchId));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<CashFlowDataDTO> getCashFlowData(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getCashFlowData(branchId));
    }

    @GetMapping("/invoices")
    public ResponseEntity<InvoiceDataDTO> getInvoiceData(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getInvoiceData(branchId));
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseDataDTO>> getExpenseData(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getExpenseData(branchId));
    }

    @GetMapping("/xero-metrics")
    public ResponseEntity<XeroMetricsDTO> getXeroMetrics(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getXeroMetrics(branchId));
    }

    @GetMapping("/payment-targets")
    public ResponseEntity<List<PaymentTargetDataDTO>> getPaymentTargetData(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getPaymentTargetData(branchId));
    }

    @GetMapping("/time-series")
    public ResponseEntity<List<TimeSeriesDataDTO>> getTimeSeriesData(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getTimeSeriesData(branchId));
    }

    @GetMapping("/rental-analytics")
    public ResponseEntity<RentalMetricsDTO> getRentalAnalytics(
            @RequestParam(required = false) Integer branchId) {
        return ResponseEntity.ok(branchService.getRentalAnalytics(branchId));
    }

    // Combined endpoint for frontend optimization
    @GetMapping("/alls")
    public ResponseEntity<DashboardSummaryDTO> getAllDashboardData(
            @RequestParam(required = false) Integer branchId) {

        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .rentalMetrics(branchService.getRentalMetrics(branchId))
                .accountsReceivable(branchService.getAccountsReceivable(branchId))
                .cashFlowData(branchService.getCashFlowData(branchId))
                .invoiceData(branchService.getInvoiceData(branchId))
                .expenseData(branchService.getExpenseData(branchId))
                .xeroMetrics(branchService.getXeroMetrics(branchId))
                .paymentTargetData(branchService.getPaymentTargetData(branchId))
                .timeSeriesData(branchService.getTimeSeriesData(branchId))
                .rentalAnalytics(branchService.getRentalAnalytics(branchId))
                .build();

        return ResponseEntity.ok(summary);
    }
}