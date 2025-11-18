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

    @NotNull(message = "Lease ID is required")
    private Long leaseId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    // Remove extra spaces and ensure proper date format
    private LocalDate paymentDate;

    private String paymentMethod;
    private String status;
    private MultipartFile proofImage;
}