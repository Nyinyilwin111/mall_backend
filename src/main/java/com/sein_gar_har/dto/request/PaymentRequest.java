package com.sein_gar_har.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {

    private Long leaseId;  // Nullable for utility payments
    private Long utilityId; // Nullable for lease payments

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;
    private MultipartFile proofImage;

    // Validation method
    public boolean isValid() {
        return (leaseId != null && utilityId == null) || (leaseId == null && utilityId != null);
    }
}