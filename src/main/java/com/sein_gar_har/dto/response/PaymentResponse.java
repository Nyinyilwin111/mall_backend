package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Payment;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentResponse {
    private Long paymentId;
    private Long leaseId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;
    private String proofImageUrl;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.leaseId = payment.getLease().getLeaseId();
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
        this.paymentMethod = payment.getPaymentMethod().toString();
        this.status = payment.getStatus().toString();
        this.proofImageUrl = payment.getProofImageUrl();
    }

    // Default constructor
    public PaymentResponse() {
    }
}