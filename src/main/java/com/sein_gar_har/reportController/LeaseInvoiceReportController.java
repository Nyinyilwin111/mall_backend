package com.sein_gar_har.reportController;

import com.sein_gar_har.Services.PaymentService;
import com.sein_gar_har.reportService.LeaseInvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/report")
@CrossOrigin(origins = "*")
public class LeaseInvoiceReportController {

    @Autowired
    private LeaseInvoiceService leaseInvoiceService;

    @GetMapping("/{paymentId}/invoice")
    public void generateInvoiceReport(
            @PathVariable Long paymentId,
            HttpServletResponse response) {

        try {
            // Set response headers for PDF download
            response.setContentType("application/pdf");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"invoice_" + paymentId + ".pdf\"");

            // Generate and stream PDF
            leaseInvoiceService.generateInvoiceReport(paymentId, response);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException("Failed to generate invoice report: " + e.getMessage(), e);
        }
    }

    // DEBUG endpoint to check parameters
    @GetMapping("/{paymentId}/test-params")
    public ResponseEntity<Map<String, Object>> testParameters(@PathVariable Long paymentId) {
        // This is just to test what parameters are being generated
        Map<String, Object> testParams = new HashMap<>();

        // Add test values
        testParams.put("subtotal", new BigDecimal("1000.00"));
        testParams.put("taxRate", new BigDecimal("5.00"));
        testParams.put("taxAmount", new BigDecimal("50.00"));
        testParams.put("totalAmount", new BigDecimal("1050.00"));

        return ResponseEntity.ok(testParams);
    }
}