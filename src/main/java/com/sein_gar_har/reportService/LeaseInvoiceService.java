package com.sein_gar_har.reportService;


import jakarta.servlet.http.HttpServletResponse;

public interface LeaseInvoiceService {
    void generateInvoiceReport(Long paymentId, HttpServletResponse response);
}
