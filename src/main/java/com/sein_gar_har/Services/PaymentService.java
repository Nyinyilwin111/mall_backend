package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.PaymentRequest;
import com.sein_gar_har.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(Long id);

    PaymentResponse updatePayment(Long id, PaymentRequest paymentRequest);

    boolean deletePayment(Long id);

    List<PaymentResponse> getPaymentsByLeaseId(Long leaseId);
}