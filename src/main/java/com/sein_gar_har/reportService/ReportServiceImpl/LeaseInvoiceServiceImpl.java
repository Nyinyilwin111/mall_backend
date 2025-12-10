package com.sein_gar_har.reportService.ReportServiceImpl;

import com.sein_gar_har.RepositoryMain.PaymentRepository;
import com.sein_gar_har.entity.Payment;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.entity.Utility;
import com.sein_gar_har.reportService.LeaseInvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LeaseInvoiceServiceImpl implements LeaseInvoiceService {

    @Autowired
    private PaymentRepository paymentRepository;

    public void generateInvoiceReport(Long paymentId, HttpServletResponse response) {
        try {
            // Get payment by ID with all relationships
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

            // Check if payment status is PAID
            if (!payment.getStatus().equals(Payment.PaymentStatus.PAID)) {
                throw new RuntimeException("Invoice can only be generated for PAID payments. Current status: " + payment.getStatus());
            }

            // Load JRXML template from resources
            ClassPathResource resource = new ClassPathResource("reports/leases/invoice_report.jrxml");
            InputStream jrxmlStream = resource.getInputStream();

            // Compile JRXML to Jasper
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            // Prepare parameters and data
            Map<String, Object> parameters = prepareInvoiceParameters(payment);
            List<Map<String, Object>> dataList = prepareReportData(payment);

            // Create data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Fill report with BOTH parameters and data source
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Set response headers for PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"invoice_" + paymentId + ".pdf\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // Export to PDF and write to response
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

            // Debug output
            System.out.println("=== DEBUG REPORT DATA ===");
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payment Amount: " + payment.getAmount());
            System.out.println("Subtotal: " + dataList.get(0).get("subtotal"));
            System.out.println("Tax Rate: " + dataList.get(0).get("taxRate"));
            System.out.println("Tax Amount: " + dataList.get(0).get("taxAmount"));
            System.out.println("Total Amount: " + dataList.get(0).get("totalAmount"));

        } catch (Exception e) {
            System.err.println("ERROR generating invoice: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate invoice report: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> prepareReportData(Payment payment) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> dataRow = new HashMap<>();

        System.out.println("=== DEBUG: Preparing Report Data ===");
        System.out.println("Payment Amount: " + payment.getAmount());

        // Add payment fields
        dataRow.put("paymentId", payment.getPaymentId());
        dataRow.put("paymentAmount", payment.getAmount());
        dataRow.put("paymentMethod", payment.getPaymentMethod() != null ?
                payment.getPaymentMethod().toString() : "N/A");
        dataRow.put("paymentStatus", payment.getStatus() != null ?
                payment.getStatus().toString() : "N/A");

        // Calculate financial fields
        BigDecimal taxRate = new BigDecimal("0.00"); // 5% tax
        BigDecimal subtotal = payment.getAmount();
        BigDecimal taxAmount = subtotal.multiply(taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal totalAmount = subtotal.add(taxAmount);

        // Debug the calculations
        System.out.println("Subtotal: " + subtotal);
        System.out.println("Tax Rate: " + taxRate);
        System.out.println("Tax Amount: " + taxAmount);
        System.out.println("Total Amount: " + totalAmount);

        // Add these as FIELDS (not parameters)
        dataRow.put("subtotal", subtotal.setScale(2, RoundingMode.HALF_UP));
        dataRow.put("taxRate", taxRate.setScale(2, RoundingMode.HALF_UP));
        dataRow.put("taxAmount", taxAmount.setScale(2, RoundingMode.HALF_UP));
        dataRow.put("totalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));

        dataList.add(dataRow);

        // Debug the data row
        System.out.println("Data Row values:");
        System.out.println("- subtotal field: " + dataRow.get("subtotal"));
        System.out.println("- taxRate field: " + dataRow.get("taxRate"));
        System.out.println("- taxAmount field: " + dataRow.get("taxAmount"));
        System.out.println("- totalAmount field: " + dataRow.get("totalAmount"));

        return dataList;
    }

    private Map<String, Object> prepareInvoiceParameters(Payment payment) {
        Map<String, Object> parameters = new HashMap<>();

        // Invoice details
        parameters.put("invoiceNumber", "INV-" + payment.getPaymentId());
        parameters.put("invoiceDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("paymentDate", payment.getPaymentDate() != null ?
                payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");

        // Payment details - these will come from fields in the report
        parameters.put("paymentId", payment.getPaymentId());
        parameters.put("paymentMethod", payment.getPaymentMethod() != null ?
                payment.getPaymentMethod().toString() : "N/A");
        parameters.put("paymentAmount", payment.getAmount());
        parameters.put("paymentStatus", payment.getStatus() != null ?
                payment.getStatus().toString() : "N/A");

        // Lease details
        if (payment.getLease() != null) {
            Lease lease = payment.getLease();
            parameters.put("leaseNumber", "LEASE-" + lease.getLeaseId());

            // User (tenant) details
            if (lease.getTenant() != null) {
                User tenant = lease.getTenant();
                parameters.put("tenantName", tenant.getFullName() != null ?
                        tenant.getFullName() : "N/A");
                parameters.put("contactPhone", lease.getContactPhone() != null ?
                        lease.getContactPhone() : "N/A");
                parameters.put("address", lease.getAddress() != null ?
                        lease.getAddress() : "N/A");
            } else {
                parameters.put("tenantName", "N/A");
                parameters.put("contactPhone", "N/A");
                parameters.put("address", "N/A");
            }

            // Space details
            if (lease.getSpace() != null) {
                Space space = lease.getSpace();
                parameters.put("roomNumber", space.getSpaceCode() != null ?
                        space.getSpaceCode() : "N/A");
                parameters.put("propertyAddress", space.getLocation() != null ?
                        space.getLocation() : "N/A");
            } else {
                parameters.put("roomNumber", "N/A");
                parameters.put("propertyAddress", "N/A");
            }

            parameters.put("rentAmount", lease.getRentAmount() != null ?
                    lease.getRentAmount() : BigDecimal.ZERO);
        } else {
            // No lease - set defaults
            parameters.put("leaseNumber", "N/A");
            parameters.put("tenantName", "N/A");
            parameters.put("contactPhone", "N/A");
            parameters.put("address", "N/A");
            parameters.put("roomNumber", "N/A");
            parameters.put("propertyAddress", "N/A");
            parameters.put("rentAmount", BigDecimal.ZERO);
        }

        // Utility details (if applicable)
        if (payment.getUtility() != null) {
            Utility utility = payment.getUtility();
            parameters.put("utilityType", utility.getUtilityType() != null ?
                    utility.getUtilityType().toString() : "");
            parameters.put("utilityDescription", utility.getDescription() != null ?
                    utility.getDescription() : "");
            parameters.put("meterReading", utility.getCurrentReading() != null ?
                    utility.getCurrentReading() : "");
            parameters.put("ratePerUnit", utility.getAmount() != null ?
                    utility.getAmount() : BigDecimal.ZERO);
        } else {
            parameters.put("utilityType", "");
            parameters.put("utilityDescription", "");
            parameters.put("meterReading", "");
            parameters.put("ratePerUnit", BigDecimal.ZERO);
        }

        // Company details
        parameters.put("companyName", "SEIN GAY HAR");
        parameters.put("companyAddress", "Parami Street, Yangon, Myanmar");
        parameters.put("companyPhone", "+95 9 123 456 789");
        parameters.put("companyEmail", "info@seingarhar.com");

        // Payment terms
        parameters.put("paymentTerms", "Due upon receipt");
        parameters.put("notes", "Thank you for your business!");

        // Report metadata
        parameters.put("COMPANY_NAME", "SEIN GAY HAR");
        parameters.put("GENERATED_BY", "System Administrator");
        parameters.put("REPORT_DATE", new Date());
        parameters.put("REPORT_ID", "INV-" + payment.getPaymentId());

        return parameters;
    }
}