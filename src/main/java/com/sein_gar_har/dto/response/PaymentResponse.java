package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Payment;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponse {
    private Long paymentId;
    private Long leaseId;
    private Long utilityId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;
    private String proofImageUrl;
    private String paymentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID spaceId;
    private String spaceCode;
    private String spaceType;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.leaseId = payment.getLease() != null ? payment.getLease().getLeaseId() : null;
        this.utilityId = payment.getUtility() != null ? payment.getUtility().getUtilityId() : null;
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
        this.paymentMethod = payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : null;
        this.status = payment.getStatus() != null ? payment.getStatus().toString() : null;
        this.proofImageUrl = payment.getProofImageUrl();
        this.paymentType = payment.getLease() != null ? "LEASE" : "UTILITY";
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();

    }

    // Default constructor
    public PaymentResponse() {
    }
}