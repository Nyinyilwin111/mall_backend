//package com.sein_gar_har.reportService;
//
//import com.sein_gar_har.dto.report.SpaceReportDTO;
//import net.sf.jasperreports.engine.JRException;
//
//import java.util.List;
//import java.util.Map;
//
//public interface SpaceReportService {
//
//    byte[] generateSpaceListReport(Map<String, Object> parameters) throws JRException;
//
//    byte[] generateSpaceDetailReport(String spaceCode, Map<String, Object> parameters) throws JRException;
//
//    byte[] generateSpaceAvailabilityReport(Map<String, Object> parameters) throws JRException;
//
//    byte[] generateSpaceByFloorReport(Integer floorId, Map<String, Object> parameters) throws JRException;
//
//    List<SpaceReportDTO> getSpaceReportData(Map<String, Object> filters);
//
//    SpaceReportDTO getSpaceDetailData(String spaceCode);
//}



package com.sein_gar_har.reportService;

import com.sein_gar_har.dto.report.SpaceReportDTO;
import net.sf.jasperreports.engine.JRException;

import java.util.List;
import java.util.Map;

public interface SpaceReportService {

    byte[] generateSpaceListReport(Map<String, Object> parameters, String format) throws JRException;

    byte[] generateSpaceDetailReport(String spaceCode, Map<String, Object> parameters, String format) throws JRException;

    byte[] generateSpaceAvailabilityReport(Map<String, Object> parameters, String format) throws JRException;

    byte[] generateSpaceByFloorReport(Integer floorId, Map<String, Object> parameters, String format) throws JRException;

    List<SpaceReportDTO> getSpaceReportData(Map<String, Object> filters);

    SpaceReportDTO getSpaceDetailData(String spaceCode);

    byte[] generateTwoPageSpaceDetailReport(String spaceCode, Map<String, Object> parameters, String format) throws JRException;
}