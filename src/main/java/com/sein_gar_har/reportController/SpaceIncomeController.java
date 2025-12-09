//package com.sein_gar_har.reportController;
//
//import com.sein_gar_har.dto.report.ReportParamsDTO;
//import com.sein_gar_har.reportService.SpaceIncomeService;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import net.sf.jasperreports.engine.JasperPrint;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/reports")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class SpaceIncomeController {
//
//    private final SpaceIncomeService jasperReportService;
//
//    @PostMapping("/income/generate")
//    public void generateIncomeReport(@RequestBody ReportParamsDTO params,
//                                     HttpServletResponse response) {
//        try {
//            jasperReportService.generateIncomeReport(params, response);
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating report", e);
//        }
//    }
//
//    @PostMapping("/income/preview")
//    public ResponseEntity<String> previewIncomeReport(@RequestBody ReportParamsDTO params) {
//        try {
//            JasperPrint jasperPrint = jasperReportService.generateReportPreview(params);
//            // Convert to HTML or return base64 encoded image
//            return ResponseEntity.ok("Preview generated successfully");
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body("Error generating preview: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/branches")
//    public ResponseEntity<?> getAllBranches() {
//        // Return list of branches for dropdown
//        return ResponseEntity.ok().build();
//    }
//}