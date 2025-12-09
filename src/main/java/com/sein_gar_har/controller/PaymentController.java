package com.sein_gar_har.controller;

import com.sein_gar_har.Services.LeaseService;
import com.sein_gar_har.Services.PaymentService;
import com.sein_gar_har.Services.SpaceService;
import com.sein_gar_har.dto.request.PaymentRequest;
import com.sein_gar_har.dto.response.LeaseResponse;
import com.sein_gar_har.dto.response.PaymentResponse;
import com.sein_gar_har.dto.response.SpaceResponseDTO;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Space;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private LeaseService leaseService;

    @Autowired
    private SpaceService spaceService;

    // ✅ CREATE LEASE PAYMENT
    @PostMapping(value = "/leasepayment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLeasePayment(@ModelAttribute PaymentRequest paymentRequest) {
        try {
            System.out.println("Received lease payment creation request:");
            System.out.println("Lease ID: " + paymentRequest.getLeaseId());
            System.out.println("Amount: " + paymentRequest.getAmount());

            PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
            return ResponseEntity.ok(createdPayment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating lease payment: " + e.getMessage());
        }
    }

    // ✅ CREATE UTILITY PAYMENT
    @PostMapping(value = "/utilitypayment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUtilityPayment(@ModelAttribute PaymentRequest paymentRequest) {
        try {
            System.out.println("Received utility payment creation request:");
            System.out.println("Utility ID: " + paymentRequest.getUtilityId());
            System.out.println("Amount: " + paymentRequest.getAmount());

            PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
            return ResponseEntity.ok(createdPayment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating utility payment: " + e.getMessage());
        }
    }

    // ✅ GET ALL PAYMENTS
    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    // ✅ GET PAYMENT BY ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable("paymentId") Long id) {
        try {
            PaymentResponse payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ GET LEASE PAYMENTS BY LEASE ID
    @GetMapping("/lease/{leaseId}/payments")
    public ResponseEntity<List<PaymentResponse>> getLeasePayments(@PathVariable Long leaseId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByLeaseId(leaseId);

            // Get lease info
            LeaseResponse lease = leaseService.getLeaseById(leaseId);

            if (lease != null && lease.getSpaceId() != null) {
                try {
                    // Get space info
                    SpaceResponseDTO space = spaceService.getSpaceById(lease.getSpaceId());

                    // Add space info to each payment
                    if (space != null) {
                        for (PaymentResponse payment : payments) {
                            payment.setSpaceId(space.getSpaceId());
                            payment.setSpaceCode(space.getSpaceCode());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Could not fetch space info for lease " + leaseId + ": " + e.getMessage());
                }
            }

            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            System.err.println("Error fetching payments for lease " + leaseId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ GET UTILITY PAYMENTS BY UTILITY ID
    @GetMapping("/utility/{utilityId}/payments")
    public ResponseEntity<List<PaymentResponse>> getUtilityPayments(@PathVariable Long utilityId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUtilityId(utilityId);
        return ResponseEntity.ok(payments);
    }

    // ✅ GET ALL PAYMENTS BY TENANT ID
    @GetMapping("/tenant/{tenantId}/all")
    public ResponseEntity<List<PaymentResponse>> getTenantPayments(@PathVariable UUID tenantId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByTenantId(tenantId);
        return ResponseEntity.ok(payments);
    }

    // ✅ GET ALL PAYMENTS BY SPACE ID
    @GetMapping("/space/{spaceId}/all")
    public ResponseEntity<List<PaymentResponse>> getSpacePayments(@PathVariable UUID spaceId) {
        List<PaymentResponse> payments = paymentService.getPaymentsBySpaceId(spaceId);
        return ResponseEntity.ok(payments);
    }

    // ✅ UPDATE PAYMENT DETAILS
    @PutMapping(value = "/updatePayment/{paymentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePaymentDetails(@PathVariable("paymentId") Long id, @ModelAttribute PaymentRequest paymentRequest) {
        try {
            System.out.println("Received payment update request for ID: " + id);

            PaymentResponse updatedPayment = paymentService.updatePayment(id, paymentRequest);
            return ResponseEntity.ok(updatedPayment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating payment: " + e.getMessage());
        }
    }

    // ✅ UPDATE PAYMENT STATUS - FIXED
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable("paymentId") Long id,
            @RequestParam String status) {
        try {
            System.out.println("Received payment status update request for ID: " + id);
            System.out.println("New Status: " + status);

            // Validate status
            if (!isValidStatus(status)) {
                return ResponseEntity.badRequest().body("Invalid status: " + status);
            }

            PaymentResponse updatedPayment = paymentService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(updatedPayment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating payment status: " + e.getMessage());
        }
    }

    private boolean isValidStatus(String status) {
        if (status == null) return false;
        String upperStatus = status.toUpperCase();
        return upperStatus.equals("PENDING") ||
                upperStatus.equals("PAID") ||
                upperStatus.equals("VERIFIED") ||
                upperStatus.equals("OVERDUE");
    }

    // ✅ DELETE PAYMENT
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> removePayment(@PathVariable("paymentId") Long id) {
        if (paymentService.deletePayment(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ GET PAYMENT SUMMARY BY LEASE
    @GetMapping("/lease/{leaseId}/summary")
    public ResponseEntity<?> getLeasePaymentSummary(@PathVariable Long leaseId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByLeaseId(leaseId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error getting payment summary: " + e.getMessage());
        }
    }

    // ✅ GET PAYMENT SUMMARY BY UTILITY
    @GetMapping("/utility/{utilityId}/summary")
    public ResponseEntity<?> getUtilityPaymentSummary(@PathVariable Long utilityId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByUtilityId(utilityId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error getting utility payment summary: " + e.getMessage());
        }
    }
}