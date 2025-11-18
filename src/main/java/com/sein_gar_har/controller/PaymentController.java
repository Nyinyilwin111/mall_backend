package com.sein_gar_har.controller;

import com.sein_gar_har.Services.PaymentService;
import com.sein_gar_har.dto.request.PaymentRequest;
import com.sein_gar_har.dto.response.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // ✅ CREATE PAYMENT
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPayment(@ModelAttribute PaymentRequest paymentRequest) {
        try {
            System.out.println("Received payment creation request:");
            System.out.println("Lease ID: " + paymentRequest.getLeaseId());
            System.out.println("Amount: " + paymentRequest.getAmount());
            System.out.println("Payment Method: " + paymentRequest.getPaymentMethod());

            PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
            return ResponseEntity.ok(createdPayment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating payment: " + e.getMessage());
        }
    }

    // ✅ GET ALL PAYMENTS
    @GetMapping("/getAll")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    // ✅ GET PAYMENT BY ID
    @GetMapping("/get/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ GET PAYMENTS BY LEASE ID
    @GetMapping("/lease/{leaseId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByLease(@PathVariable Long leaseId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByLeaseId(leaseId);
        return ResponseEntity.ok(payments);
    }

    // ✅ UPDATE PAYMENT
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePayment(@PathVariable Long id, @ModelAttribute PaymentRequest paymentRequest) {
        try {
            System.out.println("Received payment update request for ID: " + id);

            PaymentResponse updatedPayment = paymentService.updatePayment(id, paymentRequest);
            if (updatedPayment != null) {
                return ResponseEntity.ok(updatedPayment);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating payment: " + e.getMessage());
        }
    }

    // ✅ DELETE PAYMENT
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        if (paymentService.deletePayment(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}