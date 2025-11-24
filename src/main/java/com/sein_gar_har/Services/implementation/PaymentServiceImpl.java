package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.LeaseRepository;
import com.sein_gar_har.RepositoryMain.PaymentRepository;
import com.sein_gar_har.RepositoryMain.UtilityRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.LocalStorageService;
import com.sein_gar_har.Services.PaymentService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.dto.request.PaymentRequest;
import com.sein_gar_har.dto.response.PaymentResponse;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Payment;
import com.sein_gar_har.entity.Payment.PaymentStatus;
import com.sein_gar_har.entity.Utility;
import com.sein_gar_har.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private UtilityRepository utilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocalStorageService localStorageService;

    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== PAYMENT CREATE ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            if (!paymentRequest.isValid()) {
                throw new RuntimeException("Either leaseId or utilityId must be provided, but not both");
            }

            Payment payment = new Payment();

            if (paymentRequest.getLeaseId() != null) {
                Lease lease = leaseRepository.findById(paymentRequest.getLeaseId())
                        .orElseThrow(() -> new RuntimeException("Lease not found with id: " + paymentRequest.getLeaseId()));
                payment.setLease(lease);
                payment.setPaymentMethod(Payment.PaymentMethod.LEASE);
            }

            if (paymentRequest.getUtilityId() != null) {
                Utility utility = utilityRepository.findById(paymentRequest.getUtilityId())
                        .orElseThrow(() -> new RuntimeException("Utility not found with id: " + paymentRequest.getUtilityId()));
                payment.setUtility(utility);
                payment.setPaymentMethod(Payment.PaymentMethod.UTILITY);
            }

            payment.setAmount(paymentRequest.getAmount());
            payment.setPaymentDate(paymentRequest.getPaymentDate());

            if (paymentRequest.getStatus() != null) {
                try {
                    payment.setStatus(Payment.PaymentStatus.valueOf(paymentRequest.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid payment status");
                }
            } else {
                payment.setStatus(Payment.PaymentStatus.PENDING);
            }

            if (paymentRequest.getProofImage() != null && !paymentRequest.getProofImage().isEmpty()) {
                String proofImageUrl = localStorageService.saveFile(paymentRequest.getProofImage());
                payment.setProofImageUrl(proofImageUrl);
            }

            Payment savedPayment = paymentRepository.save(payment);

            Map<String, Object> newData = new HashMap<>();
            newData.put("paymentId", savedPayment.getPaymentId());
            newData.put("amount", savedPayment.getAmount());
            newData.put("paymentDate", savedPayment.getPaymentDate());
            newData.put("status", savedPayment.getStatus().toString());
            newData.put("paymentMethod", savedPayment.getPaymentMethod().toString());

            if (savedPayment.getLease() != null) {
                newData.put("leaseId", savedPayment.getLease().getLeaseId());
                newData.put("paymentType", "LEASE");
                newData.put("title", "Lease Payment");
                // Add tenant and space info for better audit log display
                if (savedPayment.getLease().getTenant() != null) {
                    newData.put("tenantName", savedPayment.getLease().getTenant().getFullName());
                }
                if (savedPayment.getLease().getSpace() != null) {
                    newData.put("spaceName", savedPayment.getLease().getSpace().getSpaceCode());
                }
            }
            if (savedPayment.getUtility() != null) {
                newData.put("utilityId", savedPayment.getUtility().getUtilityId());
                newData.put("utilityType", savedPayment.getUtility().getUtilityType());
                newData.put("paymentType", "UTILITY");
                newData.put("title", savedPayment.getUtility().getUtilityType() + " Utility Payment");
            }

            System.out.println("Audit Log Data: " + newData);
            auditLogService.logCreate("Payment", savedPayment.getPaymentId().toString(), newData);

            return convertToResponse(savedPayment);
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
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
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== PAYMENT UPDATE ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Payment existingPayment = paymentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

            Map<String, Object> oldData = new HashMap<>();
            oldData.put("paymentId", existingPayment.getPaymentId());
            oldData.put("amount", existingPayment.getAmount());
            oldData.put("paymentDate", existingPayment.getPaymentDate());
            oldData.put("status", existingPayment.getStatus().toString());
            oldData.put("paymentMethod", existingPayment.getPaymentMethod().toString());
            if (existingPayment.getLease() != null) {
                oldData.put("leaseId", existingPayment.getLease().getLeaseId());
                oldData.put("paymentType", "LEASE");
                oldData.put("title", "Lease Payment");
            }
            if (existingPayment.getUtility() != null) {
                oldData.put("utilityId", existingPayment.getUtility().getUtilityId());
                oldData.put("paymentType", "UTILITY");
                oldData.put("title", existingPayment.getUtility().getUtilityType() + " Utility Payment");
            }

            if (paymentRequest.getLeaseId() != null && paymentRequest.getUtilityId() != null) {
                throw new RuntimeException("Cannot provide both leaseId and utilityId");
            }

            if (paymentRequest.getLeaseId() != null) {
                Lease lease = leaseRepository.findById(paymentRequest.getLeaseId())
                        .orElseThrow(() -> new RuntimeException("Lease not found with id: " + paymentRequest.getLeaseId()));
                existingPayment.setLease(lease);
                existingPayment.setUtility(null);
                existingPayment.setPaymentMethod(Payment.PaymentMethod.LEASE);
            }

            if (paymentRequest.getUtilityId() != null) {
                Utility utility = utilityRepository.findById(paymentRequest.getUtilityId())
                        .orElseThrow(() -> new RuntimeException("Utility not found with id: " + paymentRequest.getUtilityId()));
                existingPayment.setUtility(utility);
                existingPayment.setLease(null);
                existingPayment.setPaymentMethod(Payment.PaymentMethod.UTILITY);
            }

            if (paymentRequest.getAmount() != null) {
                existingPayment.setAmount(paymentRequest.getAmount());
            }

            if (paymentRequest.getPaymentDate() != null) {
                existingPayment.setPaymentDate(paymentRequest.getPaymentDate());
            }

            if (paymentRequest.getStatus() != null) {
                try {
                    existingPayment.setStatus(Payment.PaymentStatus.valueOf(paymentRequest.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid payment status");
                }
            }

            if (paymentRequest.getProofImage() != null && !paymentRequest.getProofImage().isEmpty()) {
                if (existingPayment.getProofImageUrl() != null) {
                    localStorageService.deleteFile(existingPayment.getProofImageUrl());
                }
                String newProofImageUrl = localStorageService.saveFile(paymentRequest.getProofImage());
                existingPayment.setProofImageUrl(newProofImageUrl);
            }

            existingPayment.preUpdate();
            Payment updatedPayment = paymentRepository.save(existingPayment);

            Map<String, Object> newData = new HashMap<>();
            newData.put("paymentId", updatedPayment.getPaymentId());
            newData.put("amount", updatedPayment.getAmount());
            newData.put("paymentDate", updatedPayment.getPaymentDate());
            newData.put("status", updatedPayment.getStatus().toString());
            newData.put("paymentMethod", updatedPayment.getPaymentMethod().toString());
            if (updatedPayment.getLease() != null) {
                newData.put("leaseId", updatedPayment.getLease().getLeaseId());
                newData.put("paymentType", "LEASE");
                newData.put("title", "Lease Payment");
            }
            if (updatedPayment.getUtility() != null) {
                newData.put("utilityId", updatedPayment.getUtility().getUtilityId());
                newData.put("paymentType", "UTILITY");
                newData.put("title", updatedPayment.getUtility().getUtilityType() + " Utility Payment");
            }

            System.out.println("Audit Log Update Data - Old: " + oldData);
            System.out.println("Audit Log Update Data - New: " + newData);
            auditLogService.logUpdate("Payment", id.toString(), oldData, newData);

            return convertToResponse(updatedPayment);
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    @Transactional
    public boolean deletePayment(Long id) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== PAYMENT DELETE ===");
        System.out.println("Detected User: " + currentUser);
        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Optional<Payment> paymentOptional = paymentRepository.findById(id);
            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();

                Map<String, Object> oldData = new HashMap<>();
                oldData.put("paymentId", payment.getPaymentId());
                oldData.put("amount", payment.getAmount());
                oldData.put("status", payment.getStatus().toString());
                oldData.put("paymentMethod", payment.getPaymentMethod().toString());
                if (payment.getLease() != null) {
                    oldData.put("leaseId", payment.getLease().getLeaseId());
                    oldData.put("paymentType", "LEASE");
                    oldData.put("title", "Lease Payment");
                }
                if (payment.getUtility() != null) {
                    oldData.put("utilityId", payment.getUtility().getUtilityId());
                    oldData.put("paymentType", "UTILITY");
                    oldData.put("title", payment.getUtility().getUtilityType() + " Utility Payment");
                }

                if (payment.getProofImageUrl() != null) {
                    localStorageService.deleteFile(payment.getProofImageUrl());
                }

                paymentRepository.deleteById(id);

                System.out.println("Audit Log Delete Data: " + oldData);
                auditLogService.logDelete("Payment", id.toString(), oldData);

                return true;
            }
            return false;
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, String status) {
        String currentUser = getCurrentUserWithEnhancedStrategies();
        System.out.println("=== PAYMENT STATUS UPDATE ===");
        System.out.println("Payment ID: " + paymentId);
        System.out.println("New Status: " + status);
        System.out.println("Detected User: " + currentUser);

        AuditLogService.setMaintenanceOperationUser(currentUser, currentUser);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

            String oldStatus = payment.getStatus().toString();
            System.out.println("Old Status: " + oldStatus);

            PaymentStatus paymentStatus;
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status: " + status);
            }

            payment.setStatus(paymentStatus);
            payment.preUpdate();
            Payment updatedPayment = paymentRepository.save(payment);

            Map<String, Object> details = new HashMap<>();
            details.put("paymentId", paymentId);
            details.put("oldStatus", oldStatus);
            details.put("newStatus", updatedPayment.getStatus().toString());
            details.put("amount", updatedPayment.getAmount());
            details.put("paymentMethod", updatedPayment.getPaymentMethod().toString());

            if (updatedPayment.getLease() != null) {
                details.put("paymentType", "LEASE");
                details.put("leaseId", updatedPayment.getLease().getLeaseId());
                details.put("title", "Lease Payment");
            }
            if (updatedPayment.getUtility() != null) {
                details.put("paymentType", "UTILITY");
                details.put("utilityId", updatedPayment.getUtility().getUtilityId());
                details.put("utilityType", updatedPayment.getUtility().getUtilityType());
                details.put("title", updatedPayment.getUtility().getUtilityType() + " Utility Payment");
            }

            System.out.println("Audit Log Details: " + details);

            auditLogService.logAction(
                    "UPDATE",
                    "Payment",
                    paymentId.toString(),
                    null,
                    details
            );

            System.out.println("✅ Payment status updated successfully");
            return convertToResponse(updatedPayment);
        } finally {
            AuditLogService.clearMaintenanceOperationUser();
        }
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

    private PaymentResponse convertToResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponse(payment);
    }

    private String getCurrentUserWithEnhancedStrategies() {
        System.out.println("=== ENHANCED USER DETECTION ===");

        // Strategy 1: Check ThreadLocal first (for maintenance operations)
        String threadLocalUser = getCurrentUserFromThreadLocal();
        System.out.println("ThreadLocal User: " + threadLocalUser);

        if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
            return threadLocalUser;
        }

        // Strategy 2: Security Context
        String userFromSecurity = getCurrentUserFromSecurityContext();
        System.out.println("Security Context User: " + userFromSecurity);

        if (userFromSecurity != null && !userFromSecurity.trim().isEmpty() && !"System".equals(userFromSecurity)) {
            return userFromSecurity;
        }

        System.out.println("⚠️ No user detected, defaulting to System");
        return "System";
    }

    private String getCurrentUserFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Security Context - Authentication: " + authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                System.out.println("Security Context - Principal: " + principal);

                String username = extractUsernameFromPrincipal(principal);
                System.out.println("Security Context - Extracted Username: " + username);

                if (isValidUsername(username)) {
                    String fullName = findUserFullName(username);
                    if (fullName != null && !fullName.trim().isEmpty() && !"System".equals(fullName)) {
                        return fullName;
                    }
                    return username;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in Security Context strategy: " + e.getMessage());
        }
        return "System";
    }

    private String getCurrentUserFromThreadLocal() {
        try {
            String threadLocalUser = AuditLogService.getCurrentUserFromThreadLocal();
            System.out.println("ThreadLocal detection - User: " + threadLocalUser);

            if (threadLocalUser != null && !threadLocalUser.trim().isEmpty() && !"System".equals(threadLocalUser)) {
                return threadLocalUser;
            }
        } catch (Exception e) {
            System.err.println("Error in ThreadLocal strategy: " + e.getMessage());
        }
        return "System";
    }

    private String extractUsernameFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    private String findUserFullName(String username) {
        try {
            Optional<User> user = userRepository.findByEmail(username);
            if (user.isPresent()) {
                String fullName = user.get().getFullName();
                return (fullName != null && !fullName.trim().isEmpty()) ? fullName : user.get().getEmail();
            }

            user = userRepository.findByFullName(username);
            if (user.isPresent()) {
                String fullName = user.get().getFullName();
                return (fullName != null && !fullName.trim().isEmpty()) ? fullName : user.get().getEmail();
            }

            return username;
        } catch (Exception e) {
            System.err.println("Error finding user full name: " + e.getMessage());
            return username;
        }
    }

    private boolean isValidUsername(String username) {
        return username != null &&
                !username.trim().isEmpty() &&
                !"anonymousUser".equals(username) &&
                !"system".equalsIgnoreCase(username) &&
                !"null".equalsIgnoreCase(username);
    }
}