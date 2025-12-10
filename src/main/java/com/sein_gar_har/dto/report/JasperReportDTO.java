// JasperReportDTO.java
package com.sein_gar_har.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JasperReportDTO {
    private String reportName;
    private Map<String, Object> parameters;
    private ReportParamsDTO.ReportFormat format;
}