package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.LeaseRepository;
import com.sein_gar_har.RepositoryMain.PaymentRepository;
import com.sein_gar_har.Services.LocalStorageService;
import com.sein_gar_har.Services.PaymentService;
import com.sein_gar_har.dto.request.PaymentRequest;
import com.sein_gar_har.dto.response.PaymentResponse;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private LocalStorageService localStorageService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        // Find the lease
        Lease lease = leaseRepository.findById(paymentRequest.getLeaseId())
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + paymentRequest.getLeaseId()));

        // Create payment
        Payment payment = new Payment();
        payment.setLease(lease);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentDate(paymentRequest.getPaymentDate());

        // Set payment method
        if (paymentRequest.getPaymentMethod() != null && !paymentRequest.getPaymentMethod().isEmpty()) {
            try {
                payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentRequest.getPaymentMethod().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment method: " + paymentRequest.getPaymentMethod());
            }
        }

        // Set status
        if (paymentRequest.getStatus() != null && !paymentRequest.getStatus().isEmpty()) {
            try {
                payment.setStatus(Payment.PaymentStatus.valueOf(paymentRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status: " + paymentRequest.getStatus());
            }
        }

        // Upload proof image
        if (paymentRequest.getProofImage() != null && !paymentRequest.getProofImage().isEmpty()) {
            String proofImageUrl = localStorageService.saveFile(paymentRequest.getProofImage());
            payment.setProofImageUrl(proofImageUrl);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return new PaymentResponse(savedPayment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return new PaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(Long id, PaymentRequest paymentRequest) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        // Update lease if provided
        if (paymentRequest.getLeaseId() != null) {
            Lease lease = leaseRepository.findById(paymentRequest.getLeaseId())
                    .orElseThrow(() -> new RuntimeException("Lease not found with id: " + paymentRequest.getLeaseId()));
            existingPayment.setLease(lease);
        }

        // Update other fields
        if (paymentRequest.getAmount() != null) {
            existingPayment.setAmount(paymentRequest.getAmount());
        }
        if (paymentRequest.getPaymentDate() != null) {
            existingPayment.setPaymentDate(paymentRequest.getPaymentDate());
        }

        // Update payment method
        if (paymentRequest.getPaymentMethod() != null && !paymentRequest.getPaymentMethod().isEmpty()) {
            try {
                existingPayment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentRequest.getPaymentMethod().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment method: " + paymentRequest.getPaymentMethod());
            }
        }

        // Update status
        if (paymentRequest.getStatus() != null && !paymentRequest.getStatus().isEmpty()) {
            try {
                existingPayment.setStatus(Payment.PaymentStatus.valueOf(paymentRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status: " + paymentRequest.getStatus());
            }
        }

        // Handle proof image update
        if (paymentRequest.getProofImage() != null && !paymentRequest.getProofImage().isEmpty()) {
            // Delete old proof image if exists
            if (existingPayment.getProofImageUrl() != null && !existingPayment.getProofImageUrl().isEmpty()) {
                localStorageService.deleteFile(existingPayment.getProofImageUrl());
            }
            // Upload new proof image
            String newProofImageUrl = localStorageService.saveFile(paymentRequest.getProofImage());
            existingPayment.setProofImageUrl(newProofImageUrl);
        }

        existingPayment.preUpdate();
        Payment updatedPayment = paymentRepository.save(existingPayment);
        return new PaymentResponse(updatedPayment);
    }

    @Override
    @Transactional
    public boolean deletePayment(Long id) {
        Optional<Payment> paymentOptional = paymentRepository.findById(id);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();

            // Delete proof image from storage if exists
            if (payment.getProofImageUrl() != null && !payment.getProofImageUrl().isEmpty()) {
                localStorageService.deleteFile(payment.getProofImageUrl());
            }

            paymentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<PaymentResponse> getPaymentsByLeaseId(Long leaseId) {
        return paymentRepository.findAllByLeaseId(leaseId)
                .stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }
}