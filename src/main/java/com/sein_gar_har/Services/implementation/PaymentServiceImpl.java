package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.LeaseRepository;
import com.sein_gar_har.RepositoryMain.PaymentRepository;
import com.sein_gar_har.RepositoryMain.UtilityRepository;
import com.sein_gar_har.Services.LocalStorageService;
import com.sein_gar_har.Services.PaymentService;
import com.sein_gar_har.dto.request.PaymentRequest;
import com.sein_gar_har.dto.response.PaymentResponse;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Payment;
import com.sein_gar_har.entity.Payment.PaymentStatus;
import com.sein_gar_har.entity.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private UtilityRepository utilityRepository;

    @Autowired
    private LocalStorageService localStorageService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        // Validate that either lease or utility is provided, but not both
        if (!paymentRequest.isValid()) {
            throw new RuntimeException("Either leaseId or utilityId must be provided, but not both");
        }

        Payment payment = new Payment();

        // Handle lease payment
        if (paymentRequest.getLeaseId() != null) {
            Lease lease = leaseRepository.findById(paymentRequest.getLeaseId())
                    .orElseThrow(() -> new RuntimeException("Lease not found with id: " + paymentRequest.getLeaseId()));
            payment.setLease(lease);
            payment.setPaymentMethod(Payment.PaymentMethod.LEASE);
        }

        // Handle utility payment
        if (paymentRequest.getUtilityId() != null) {
            Utility utility = utilityRepository.findById(paymentRequest.getUtilityId())
                    .orElseThrow(() -> new RuntimeException("Utility not found with id: " + paymentRequest.getUtilityId()));
            payment.setUtility(utility);
            payment.setPaymentMethod(Payment.PaymentMethod.UTILITY);
        }

        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentDate(paymentRequest.getPaymentDate());

        // Set status
        if (paymentRequest.getStatus() != null) {
            try {
                payment.setStatus(Payment.PaymentStatus.valueOf(paymentRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status");
            }
        } else {
            payment.setStatus(Payment.PaymentStatus.PENDING); // Default status
        }

        // Upload proof image
        if (paymentRequest.getProofImage() != null && !paymentRequest.getProofImage().isEmpty()) {
            String proofImageUrl = localStorageService.saveFile(paymentRequest.getProofImage());
            payment.setProofImageUrl(proofImageUrl);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return convertToResponse(savedPayment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return convertToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(Long id, PaymentRequest paymentRequest) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        // Validate update request - allow partial updates, so don't validate strictly
        if (paymentRequest.getLeaseId() != null && paymentRequest.getUtilityId() != null) {
            throw new RuntimeException("Cannot provide both leaseId and utilityId");
        }

        // Handle lease update
        if (paymentRequest.getLeaseId() != null) {
            Lease lease = leaseRepository.findById(paymentRequest.getLeaseId())
                    .orElseThrow(() -> new RuntimeException("Lease not found with id: " + paymentRequest.getLeaseId()));
            existingPayment.setLease(lease);
            existingPayment.setUtility(null); // Clear utility if switching to lease payment
            existingPayment.setPaymentMethod(Payment.PaymentMethod.LEASE);
        }

        // Handle utility update
        if (paymentRequest.getUtilityId() != null) {
            Utility utility = utilityRepository.findById(paymentRequest.getUtilityId())
                    .orElseThrow(() -> new RuntimeException("Utility not found with id: " + paymentRequest.getUtilityId()));
            existingPayment.setUtility(utility);
            existingPayment.setLease(null); // Clear lease if switching to utility payment
            existingPayment.setPaymentMethod(Payment.PaymentMethod.UTILITY);
        }

        if (paymentRequest.getAmount() != null) {
            existingPayment.setAmount(paymentRequest.getAmount());
        }

        if (paymentRequest.getPaymentDate() != null) {
            existingPayment.setPaymentDate(paymentRequest.getPaymentDate());
        }

        // Update status
        if (paymentRequest.getStatus() != null) {
            try {
                existingPayment.setStatus(Payment.PaymentStatus.valueOf(paymentRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status");
            }
        }

        // Update proof image
        if (paymentRequest.getProofImage() != null && !paymentRequest.getProofImage().isEmpty()) {
            if (existingPayment.getProofImageUrl() != null) {
                localStorageService.deleteFile(existingPayment.getProofImageUrl());
            }

            String newProofImageUrl = localStorageService.saveFile(paymentRequest.getProofImage());
            existingPayment.setProofImageUrl(newProofImageUrl);
        }

        existingPayment.preUpdate();
        Payment updatedPayment = paymentRepository.save(existingPayment);
        return convertToResponse(updatedPayment);
    }

    @Override
    @Transactional
    public boolean deletePayment(Long id) {
        Optional<Payment> paymentOptional = paymentRepository.findById(id);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();

            if (payment.getProofImageUrl() != null) {
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
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByUtilityId(Long utilityId) {
        return paymentRepository.findAllByUtilityId(utilityId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByTenantId(UUID tenantId) {
        return paymentRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsBySpaceId(UUID spaceId) {
        return paymentRepository.findAllBySpaceId(spaceId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, String status) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

            // Validate and set status
            PaymentStatus paymentStatus;
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status: " + status);
            }

            payment.setStatus(paymentStatus);
            payment.preUpdate(); // Update timestamp
            Payment updatedPayment = paymentRepository.save(payment);

            return convertToResponse(updatedPayment);
        } catch (Exception e) {
            throw new RuntimeException("Error updating payment status: " + e.getMessage(), e);
        }
    }

    // FIXED convertToResponse method - matches your PaymentResponse DTO structure
    private PaymentResponse convertToResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        // Use the constructor approach that matches your DTO
        return new PaymentResponse(payment);

        // Alternative manual conversion if needed:
        /*
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setLeaseId(payment.getLease() != null ? payment.getLease().getLeaseId() : null);
        response.setUtilityId(payment.getUtility() != null ? payment.getUtility().getUtilityId() : null);
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : null);
        response.setStatus(payment.getStatus() != null ? payment.getStatus().toString() : null);
        response.setProofImageUrl(payment.getProofImageUrl());
        response.setPaymentType(payment.getLease() != null ? "LEASE" : "UTILITY");
        return response;
        */
    }
}