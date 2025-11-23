package com.sein_gar_har.reportService;

import com.sein_gar_har.dto.reports.SpaceReportDTO;
import net.sf.jasperreports.engine.JRException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SpaceReportService {

    byte[] generateSpaceListReport(Map<String, Object> parameters) throws JRException;

    byte[] generateSpaceDetailReport(String spaceCode, Map<String, Object> parameters) throws JRException;

    byte[] generateSpaceAvailabilityReport(Map<String, Object> parameters) throws JRException;

    byte[] generateSpaceByFloorReport(Integer floorId, Map<String, Object> parameters) throws JRException;

    List<SpaceReportDTO> getSpaceReportData(Map<String, Object> filters);

    SpaceReportDTO getSpaceDetailData(String spaceCode);
}