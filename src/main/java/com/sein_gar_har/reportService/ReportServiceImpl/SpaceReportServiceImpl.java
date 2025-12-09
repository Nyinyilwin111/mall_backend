package com.sein_gar_har.reportService.ReportServiceImpl;

import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.dto.report.AmenityItem;
import com.sein_gar_har.dto.report.SpaceReportDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.reportService.SpaceReportService;
import com.sein_gar_har.reportService.reportUtil.ReportCompiler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpaceReportServiceImpl implements SpaceReportService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    @Qualifier("mainDataSource")
    private DataSource dataSource;

    @Autowired
    BranchService branchService;

    @Autowired
    private ReportCompiler reportCompiler;

    @Autowired
    private ResourceLoader resourceLoader;

    // Supported image formats by JasperReports
    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // Formats that need conversion to be displayed
    private static final Set<String> CONVERSION_FORMATS = Set.of(
            "avif", "heic", "heif", "tiff", "tif", "raw", "cr2", "nef", "arw"
    );

    @Override
    public byte[] generateSpaceDetailReport(String spaceCode, Map<String, Object> parameters, String format) throws JRException {
        try {
            // Get space data
            SpaceReportDTO spaceDetail = getSpaceDetailData(spaceCode);
            if (spaceDetail == null) {
                throw new JRException("Space not found with code: " + spaceCode);
            }

            // Prepare data list for JasperReports
            List<SpaceReportDTO> dataList = Collections.singletonList(spaceDetail);

            // Use ReportCompiler to get compiled reports
            JasperReport jasperReport = reportCompiler.getCompiledReport("space_detail_report");
            JasperReport amenitiesSubreport = reportCompiler.getCompiledReport("amenities_subreport");

            // Add parameters
            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "SPACE DETAIL REPORT");
            parameters.put("spaceCode", spaceCode);
            parameters.put("HAS_IMAGES", spaceDetail.getImageUrls() != null && !spaceDetail.getImageUrls().isEmpty());
            parameters.put("TOTAL_IMAGES", spaceDetail.getImageCount());
            parameters.put("IMAGE_GALLERY_TEXT", generateImageGalleryText(spaceDetail.getImageCount()));

            // Create data source for amenities subreport
            List<AmenityItem> amenityItems = spaceDetail.getAmenityItems() != null ?
                    spaceDetail.getAmenityItems() : new ArrayList<>();
            JRDataSource amenitiesDataSource = new JRBeanCollectionDataSource(amenityItems);

            // Add compiled subreport and data source to parameters
            parameters.put("amenitiesSubreport", amenitiesSubreport);
            parameters.put("amenitiesDataSource", amenitiesDataSource);

            // Use Bean Collection Data Source for main report
            JRDataSource jrDataSource = new JRBeanCollectionDataSource(dataList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrDataSource);

            return exportReportToFormat(jasperPrint, format);

        } catch (Exception e) {
            log.error("Error generating space detail report for spaceCode: {}", spaceCode, e);
            throw new JRException("Failed to generate space detail report: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateTwoPageSpaceDetailReport(String spaceCode, Map<String, Object> parameters, String format) throws JRException {
        if (!"pdf".equalsIgnoreCase(format)) {
            throw new JRException("Two-page report is only supported in PDF format");
        }

        try {
            // Get space data
            SpaceReportDTO spaceDetail = getSpaceDetailData(spaceCode);
            if (spaceDetail == null) {
                throw new JRException("Space not found with code: " + spaceCode);
            }

            // Prepare data list for JasperReports
            List<SpaceReportDTO> dataList = Collections.singletonList(spaceDetail);
            JRDataSource mainDataSource = new JRBeanCollectionDataSource(dataList);

            // Use ReportCompiler to get compiled reports
            JasperReport mainReport = reportCompiler.getCompiledReport("space_detail_report");
            JasperReport amenitiesSubreport = reportCompiler.getCompiledReport("amenities_subreport");

            // Add parameters
            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "SPACE DETAIL REPORT");
            parameters.put("spaceCode", spaceCode);
            parameters.put("HAS_IMAGES", spaceDetail.getImageUrls() != null && !spaceDetail.getImageUrls().isEmpty());
            parameters.put("TOTAL_IMAGES", spaceDetail.getImageCount());
            parameters.put("IMAGE_GALLERY_TEXT", generateImageGalleryText(spaceDetail.getImageCount()));

            // Create data source for amenities subreport
            List<AmenityItem> amenityItems = spaceDetail.getAmenityItems() != null ?
                    spaceDetail.getAmenityItems() : new ArrayList<>();
            JRDataSource amenitiesDataSource = new JRBeanCollectionDataSource(amenityItems);

            // Add compiled subreport and data source to parameters
            parameters.put("amenitiesSubreport", amenitiesSubreport);
            parameters.put("amenitiesDataSource", amenitiesDataSource);

            // Fill the main report (which now includes both pages in summary)
            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReport, parameters, mainDataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            log.error("Error generating two-page space detail report for spaceCode: {}", spaceCode, e);
            throw new JRException("Failed to generate two-page space detail report: " + e.getMessage(), e);
        }
    }

    private SpaceReportDTO convertToPremiumReportDTO(Space space) {
        // Extract and process image URLs with format validation and conversion
        List<String> imageUrls = processImageUrls(space.getImages());

        String description = generateSpaceDescription(space);
        String specialFeatures = generateSpecialFeatures(space);

        // Parse amenities from JSON and format for display
        List<String> amenityStrings = parseAmenitiesFromJson(space.getAmenities());

        // Convert String list to AmenityItem list for subreport
        List<AmenityItem> amenityItems = amenityStrings.stream()
                .map(AmenityItem::new)
                .collect(Collectors.toList());

        String formattedAmenities = formatAmenitiesForDisplay(amenityStrings);

        return SpaceReportDTO.builder()
                .spaceId(space.getSpaceId() != null ? space.getSpaceId().toString() : "N/A")
                .spaceCode(space.getSpaceCode())
                .location(space.getLocation())
                .sizeSqft(space.getSizeSqft())
                .price(space.getPrice() != null ? BigDecimal.valueOf(space.getPrice()) : BigDecimal.ZERO)
                .amenities(formattedAmenities)
                .amenityList(amenityStrings)
                .amenityItems(amenityItems)
                .status(space.getStatus() != null ? space.getStatus().name() : "VACANT")
                .spaceType(space.getSpaceType() != null ? space.getSpaceType().getTypeName() : "Space")
                .floorLevel(space.getFloor() != null ?
                        (space.getFloor().getLevel() != null ? space.getFloor().getLevel() : "N/A") : "N/A")
                .branchName(getPremiumBranchName(space))
                .createdAt(space.getCreatedAt())
                .imageCount(imageUrls.size())
                .imageUrls(imageUrls)
                .description(description)
                .contactPerson("Sein Gar Har Property Manager")
                .contactPhone("09798751111 | 09784425961")
                .contactEmail("info@seingarhar.com")
                .availabilityStatus(getAvailabilityStatus(space.getStatus()))
                .specialFeatures(specialFeatures)
                .build();
    }

    /**
     * Parse amenities from JSON format like {"gg":"true", "hhh":"true"}
     */
    private List<String> parseAmenitiesFromJson(String amenitiesJson) {
        List<String> amenities = new ArrayList<>();

        if (amenitiesJson == null || amenitiesJson.trim().isEmpty()) {
            return amenities;
        }

        String trimmed = amenitiesJson.trim();

        // Check if it's JSON format
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                // Simple JSON parsing - extract keys
                String content = trimmed.substring(1, trimmed.length() - 1);
                String[] pairs = content.split("\\s*,\\s*");

                for (String pair : pairs) {
                    String[] keyValue = pair.split("\\s*:\\s*");
                    if (keyValue.length >= 1) {
                        String key = keyValue[0].replaceAll("^\"|\"$", "").trim();
                        if (!key.isEmpty()) {
                            amenities.add(key);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse amenities JSON: {}, using as simple text", amenitiesJson, e);
                // Fallback: treat as simple text
                amenities.add(amenitiesJson);
            }
        } else {
            // Not JSON, treat as simple text
            amenities.add(amenitiesJson);
        }

        return amenities;
    }

    private String formatAmenitiesForDisplay(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "Standard amenities included";
        }
        return String.join(", ", amenities);
    }

    private List<String> processImageUrls(List<String> originalImageUrls) {
        if (originalImageUrls == null || originalImageUrls.isEmpty()) {
            log.info("No images found for space");
            return new ArrayList<>();
        }

        List<String> processedUrls = new ArrayList<>();
        int supportedCount = 0;
        int convertedCount = 0;
        int invalidCount = 0;

        for (String imageUrl : originalImageUrls) {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                invalidCount++;
                continue;
            }

            ImageProcessingResult result = processImageUrl(imageUrl);
            if (result.isSuccess()) {
                processedUrls.add(result.getProcessedUrl());
                if (result.isConverted()) {
                    convertedCount++;
                } else {
                    supportedCount++;
                }
            } else {
                invalidCount++;
                // Add placeholder for invalid images
                processedUrls.add(createImagePlaceholder("Invalid image: " + imageUrl));
            }
        }

        log.info("Image processing completed - Supported: {}, Converted: {}, Invalid: {}, Total: {}",
                supportedCount, convertedCount, invalidCount, originalImageUrls.size());

        return processedUrls;
    }

    private ImageProcessingResult processImageUrl(String imageUrl) {
        try {
            // Check if it's a data URL (base64) - already in compatible format
            if (imageUrl.startsWith("data:image")) {
                log.debug("Using base64 image data URL");
                return new ImageProcessingResult(imageUrl, false);
            }

            // Check file extension
            String fileExtension = getFileExtension(imageUrl).toLowerCase();

            // Handle unsupported formats by converting to JPEG
            if (CONVERSION_FORMATS.contains(fileExtension)) {
                log.info("Converting unsupported format: {} for URL: {}", fileExtension, imageUrl);
                String convertedUrl = convertImageToJpeg(imageUrl);
                if (convertedUrl != null) {
                    return new ImageProcessingResult(convertedUrl, true);
                } else {
                    log.warn("Failed to convert image: {}", imageUrl);
                    return ImageProcessingResult.failed();
                }
            }

            // For supported formats, ensure accessibility
            if (SUPPORTED_FORMATS.contains(fileExtension)) {
                if (isImageAccessible(imageUrl)) {
                    log.debug("Image accessible: {}", imageUrl);
                    return new ImageProcessingResult(imageUrl, false);
                } else {
                    log.warn("Image not accessible: {}", imageUrl);
                    return ImageProcessingResult.failed();
                }
            }

            // For unknown formats, try to process anyway
            log.warn("Unknown image format: {} for URL: {}, attempting to process", fileExtension, imageUrl);
            String convertedUrl = convertImageToJpeg(imageUrl);
            if (convertedUrl != null) {
                return new ImageProcessingResult(convertedUrl, true);
            }

            return ImageProcessingResult.failed();

        } catch (Exception e) {
            log.error("Error processing image URL: {}", imageUrl, e);
            return ImageProcessingResult.failed();
        }
    }

    private String convertImageToJpeg(String imageUrl) {
        try {
            // Download the image bytes
            byte[] imageBytes = downloadImageBytes(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Failed to download image for conversion: {}", imageUrl);
                return null;
            }

            // Try to read the image with ImageIO
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);

            if (image != null) {
                // Convert to JPEG base64
                return convertToBase64JPEG(image);
            } else {
                log.warn("ImageIO could not read image from URL: {}", imageUrl);
                // Try creating a placeholder
                return createImagePlaceholder("Unsupported format: " + getFileExtension(imageUrl));
            }

        } catch (Exception e) {
            log.error("Error converting image to JPEG: {}", imageUrl, e);
            return createImagePlaceholder("Conversion failed: " + imageUrl);
        }
    }

    private String convertToBase64JPEG(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Convert to RGB if necessary (handle transparency)
            BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            rgbImage.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);

            // Write as JPEG
            ImageIO.write(rgbImage, "JPEG", outputStream);
            byte[] jpegBytes = outputStream.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(jpegBytes);
            return "data:image/jpeg;base64," + base64;
        } catch (Exception e) {
            log.error("Error converting image to base64 JPEG", e);
            return null;
        }
    }

    private String createImagePlaceholder(String message) {
        try {
            // Create a simple placeholder image
            BufferedImage placeholder = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = placeholder.createGraphics();

            // Set background
            g2d.setColor(java.awt.Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, 300, 200);

            // Draw border
            g2d.setColor(java.awt.Color.DARK_GRAY);
            g2d.drawRect(0, 0, 299, 199);

            // Draw text
            g2d.setColor(java.awt.Color.DARK_GRAY);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));

            // Split long messages
            String[] lines = splitMessage(message, 30);
            for (int i = 0; i < lines.length; i++) {
                g2d.drawString(lines[i], 10, 90 + (i * 20));
            }

            g2d.dispose();
            return convertToBase64JPEG(placeholder);
        } catch (Exception e) {
            log.error("Error creating image placeholder", e);
            // Fallback to a simple gray image
            return "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=";
        }
    }

    private String[] splitMessage(String message, int maxLength) {
        List<String> lines = new ArrayList<>();
        while (message.length() > maxLength) {
            int breakPoint = message.lastIndexOf(' ', maxLength);
            if (breakPoint == -1) breakPoint = maxLength;
            lines.add(message.substring(0, breakPoint));
            message = message.substring(breakPoint).trim();
        }
        lines.add(message);
        return lines.toArray(new String[0]);
    }

    private byte[] downloadImageBytes(String imageUrl) {
        try {
            if (imageUrl.startsWith("data:")) {
                // Extract base64 data
                String base64Data = imageUrl.split(",")[1];
                return Base64.getDecoder().decode(base64Data);
            }

            if (imageUrl.startsWith("classpath:")) {
                String resourcePath = imageUrl.substring("classpath:".length());
                InputStream inputStream = new ClassPathResource(resourcePath).getInputStream();
                return inputStream.readAllBytes();
            }

            // HTTP/HTTPS URL
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 JasperReports");

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }

        } catch (Exception e) {
            log.warn("Failed to download image bytes: {}", imageUrl, e);
            return null;
        }
    }

    private String getFileExtension(String url) {
        try {
            // Remove query parameters
            String cleanUrl = url.split("\\?")[0];
            // Extract extension
            return FilenameUtils.getExtension(cleanUrl).toLowerCase();
        } catch (Exception e) {
            log.warn("Could not extract extension from URL: {}", url);
            return "unknown";
        }
    }

    private boolean isImageAccessible(String imageUrl) {
        if (imageUrl.startsWith("data:image")) {
            return true;
        }

        if (imageUrl.startsWith("classpath:")) {
            try {
                String resourcePath = imageUrl.substring("classpath:".length());
                InputStream stream = new ClassPathResource(resourcePath).getInputStream();
                stream.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        if (imageUrl.startsWith("http")) {
            return testHttpImageAccessibility(imageUrl);
        }

        if (imageUrl.startsWith("/") || imageUrl.contains(":\\")) {
            File file = new File(imageUrl);
            return file.exists() && file.isFile() && file.length() > 0;
        }

        return false;
    }

    private boolean testHttpImageAccessibility(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 JasperReports");

            try (InputStream stream = connection.getInputStream()) {
                return true;
            }
        } catch (Exception e) {
            log.debug("HTTP image accessibility test failed for: {}", imageUrl);
            return false;
        }
    }

    private String generateSpaceDescription(Space space) {
        StringBuilder description = new StringBuilder();
        description.append("Commercial space");

        if (space.getLocation() != null) {
            description.append(" located in ").append(space.getLocation());
        }

        if (space.getSizeSqft() != null) {
            description.append(". This ").append(String.format("%,.0f", space.getSizeSqft()))
                    .append(" sq.ft. space");
        }

        if (space.getSpaceType() != null) {
            description.append(" is ideal for ").append(space.getSpaceType().getTypeName().toLowerCase());
        }

        description.append(". Features excellent amenities and prime location for business success.");

        return description.toString();
    }

    private String generateSpecialFeatures(Space space) {
        List<String> features = new ArrayList<>();

        // Add basic features
        features.add("24/7 Security Access");
        features.add("CCTV Surveillance");
        features.add("Professional Maintenance");

        // Add amenities-based features
        if (space.getAmenities() != null) {
            if (space.getAmenities().toLowerCase().contains("air")) {
                features.add("Air Conditioning");
            }
            if (space.getAmenities().toLowerCase().contains("park")) {
                features.add("Dedicated Parking");
            }
            if (space.getAmenities().toLowerCase().contains("wifi") ||
                    space.getAmenities().toLowerCase().contains("internet")) {
                features.add("High-Speed Internet");
            }
        }

        // Add space type specific features
        if (space.getSpaceType() != null) {
            String typeName = space.getSpaceType().getTypeName().toLowerCase();
            if (typeName.contains("office")) {
                features.add("Professional Office Setup");
            } else if (typeName.contains("retail")) {
                features.add("Prime Retail Location");
            } else if (typeName.contains("storage")) {
                features.add("Secure Storage Facility");
            }
        }

        return String.join(", ", features);
    }

    private String getAvailabilityStatus(Space.SpaceStatus status) {
        if (status == null) return "Availability Unknown";

        return switch (status) {
            case VACANT -> "Immediately Available";
            case OCCUPIED -> "Currently Occupied";
            case MAINTENANCE -> "Under Renovation";
            case RESERVED -> "Reserved - Available Soon";
            default -> "Check Availability";
        };
    }

    private String getPremiumBranchName(Space space) {
        if (space.getFloor() != null && space.getFloor().getBranchBranchId() != null) {
            try {
                Long branchId = Long.valueOf(space.getFloor().getBranchBranchId());
                BranchResponseDTO branch = branchService.getBranchById(branchId);
                return branch != null ? branch.getName() : "Branch not found";
            } catch (Exception e) {
                log.error("Error getting branch name for branch ID: {}", space.getFloor().getBranchBranchId(), e);
                return "Branch information unavailable";
            }
        }
        return "Branch not assigned";
    }

    private String generateImageGalleryText(int imageCount) {
        if (imageCount == 0) {
            return "No images available for this space";
        } else if (imageCount == 1) {
            return "1 image available";
        } else {
            return imageCount + " images available";
        }
    }

    private void addPremiumParameters(Map<String, Object> parameters) {
        // Use putIfAbsent instead of put to avoid overwriting existing parameters
        parameters.putIfAbsent("COMPANY_NAME", "Sein Gar Har");
        parameters.putIfAbsent("COMPANY_SLOGAN", "Quality Properties, Exceptional Service");
        parameters.putIfAbsent("REPORT_DATE", new Date());
        parameters.putIfAbsent("GENERATED_BY", "Report System");
        parameters.putIfAbsent("REPORT_TIMEZONE", "Asia/Yangon");
        parameters.putIfAbsent("WATERMARK_TEXT", "CONFIDENTIAL");
        parameters.putIfAbsent("CONTACT_PERSON", "Sein Gar Har Mall Manager");
        parameters.putIfAbsent("CONTACT_EMAIL", "info@seingarhar.com");
        parameters.putIfAbsent("CONTACT_PHONE", "09798751111 | 09784425961");
        parameters.putIfAbsent("WEBSITE", "www.seingarhar.com");
        parameters.putIfAbsent("REPORT_VERSION", "1.0");
        parameters.putIfAbsent("REPORT_STYLE", "STANDARD");
        parameters.putIfAbsent("SUBREPORT_DIR", "");

        // FIXED: Check if IS_IGNORE_PAGINATION already exists before adding
        if (!parameters.containsKey("IS_IGNORE_PAGINATION")) {
            parameters.put("IS_IGNORE_PAGINATION", Boolean.FALSE);
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Error closing database connection", e);
            }
        }
    }

    private void closeStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                log.error("Error closing input stream", e);
            }
        }
    }

    // Helper class for image processing results
    @Getter
    private static class ImageProcessingResult {
        private final String processedUrl;
        private final boolean converted;
        private final boolean success;

        public ImageProcessingResult(String processedUrl, boolean converted) {
            this.processedUrl = processedUrl;
            this.converted = converted;
            this.success = true;
        }

        private ImageProcessingResult() {
            this.processedUrl = null;
            this.converted = false;
            this.success = false;
        }

        public static ImageProcessingResult failed() {
            return new ImageProcessingResult();
        }
    }

    @Override
    public List<SpaceReportDTO> getSpaceReportData(Map<String, Object> filters) {
        List<Space> spaces = spaceRepository.findAll();
        return spaces.stream()
                .filter(space -> applyFilters(space, filters))
                .map(this::convertToPremiumReportDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceReportDTO getSpaceDetailData(String spaceCode) {
        try {
            Optional<Space> space = spaceRepository.findBySpaceCode(spaceCode);
            if (space.isEmpty()) {
                log.warn("Space not found with code: {}", spaceCode);
                return null;
            }
            return convertToPremiumReportDTO(space.get());
        } catch (Exception e) {
            log.error("Error getting space detail data for code: {}", spaceCode, e);
            return null;
        }
    }

    private boolean applyFilters(Space space, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }

        if (filters.containsKey("status") && space.getStatus() != null) {
            String filterStatus = (String) filters.get("status");
            if (!space.getStatus().name().equalsIgnoreCase(filterStatus)) {
                return false;
            }
        }

        if (filters.containsKey("floorId") && space.getFloor() != null) {
            Integer filterFloorId = (Integer) filters.get("floorId");
            if (!space.getFloor().getFloorId().equals(filterFloorId)) {
                return false;
            }
        }

        if (filters.containsKey("spaceTypeId") && space.getSpaceType() != null) {
            UUID filterSpaceTypeId = (UUID) filters.get("spaceTypeId");
            if (!space.getSpaceType().getSpaceTypeId().equals(filterSpaceTypeId)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public byte[] generateSpaceListReport(Map<String, Object> parameters, String format) throws JRException {
        Connection connection = null;
        InputStream logoStream = null;

        try {
            // Clear cache and retry if compilation fails
            try {
                reportCompiler.getCompiledReport("space_list_report");
            } catch (RuntimeException e) {
                log.error("❌ First compilation attempt failed, clearing cache: {}", e.getMessage());
                reportCompiler.clearReportCache("space_list_report");
                // Retry compilation
                reportCompiler.getCompiledReport("space_list_report");
            }

            connection = dataSource.getConnection();
            JasperReport jasperReport = reportCompiler.getCompiledReport("space_list_report");

            // Normalize parameters
            Map<String, Object> jasperParams = new HashMap<>();

            // Copy all parameters
            if (parameters != null) {
                jasperParams.putAll(parameters);
            }

            // Normalize status to uppercase if present
            if (jasperParams.containsKey("status") && jasperParams.get("status") instanceof String) {
                String status = ((String) jasperParams.get("status")).toUpperCase();
                jasperParams.put("status", status);
            }

            // Ensure null values are handled - set empty strings instead of null
            jasperParams.putIfAbsent("status", "");
            jasperParams.putIfAbsent("spaceTypeId", "");
            jasperParams.putIfAbsent("branchId", "");
            jasperParams.putIfAbsent("spaceType", "");
            jasperParams.putIfAbsent("branch", "");

            // Add premium parameters
            addPremiumParameters(jasperParams);
            jasperParams.put("REPORT_TITLE", "SPACE LIST REPORT");

            // Load logo
            logoStream = loadLogoWithMultipleMethods();
            if (logoStream != null) {
                jasperParams.put("COMPANY_LOGO", logoStream);
            }

            log.info("🔍 Executing space list report with parameters: status={}, spaceTypeId={}, branchId={}, spaceType={}, branch={}",
                    jasperParams.get("status"),
                    jasperParams.get("spaceTypeId"),
                    jasperParams.get("branchId"),
                    jasperParams.get("spaceType"),
                    jasperParams.get("branch"));

            // Log all parameters for debugging
            log.debug("📋 All report parameters:");
            jasperParams.forEach((key, value) -> {
                log.debug("  {}: {} (type: {})",
                        key,
                        value != null ? value.toString() : "null",
                        value != null ? value.getClass().getSimpleName() : "null");
            });

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParams, connection);

            log.info("📊 JasperPrint pages: {}", jasperPrint.getPages().size());
            log.info("✅ Report generated successfully");

            byte[] result = exportReportToFormat(jasperPrint, format);
            return result;

        } catch (JRException e) {
            log.error("❌ JasperReports error generating space list report", e);
            // Clear cache and retry once
            reportCompiler.clearReportCache("space_list_report");
            throw new JRException("Failed to generate space list report: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ General error generating space list report", e);
            throw new JRException("Failed to generate space list report: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStream(logoStream);
        }
    }

    // ✅ FIXED: Improved logo loading with multiple methods
    private InputStream loadLogoWithMultipleMethods() {
        String[] possiblePaths = {
                "classpath:image/SGH-logo.png",
                "classpath:/image/SGH-logo.png",
                "image/SGH-logo.png",
                "/image/SGH-logo.png",
                "src/main/resources/image/SGH-logo.png",
                "static/image/SGH-logo.png",
                "classpath:static/image/SGH-logo.png"
        };

        for (String path : possiblePaths) {
            try {
                log.info("🔍 Trying logo path: {}", path);
                InputStream stream = null;

                if (path.startsWith("classpath:")) {
                    String resourcePath = path.substring("classpath:".length());
                    stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
                    if (stream == null) {
                        stream = getClass().getResourceAsStream(resourcePath);
                    }
                    if (stream == null && resourceLoader != null) {
                        Resource resource = resourceLoader.getResource(path);
                        if (resource.exists()) {
                            stream = resource.getInputStream();
                        }
                    }
                } else if (path.startsWith("src/")) {
                    // Direct file system access
                    File logoFile = new File(path);
                    if (logoFile.exists() && logoFile.isFile() && logoFile.length() > 0) {
                        stream = new FileInputStream(logoFile);
                    }
                } else {
                    // Try as classpath resource
                    stream = getClass().getClassLoader().getResourceAsStream(path);
                    if (stream == null) {
                        stream = getClass().getResourceAsStream(path);
                    }
                    if (stream == null && !path.startsWith("/")) {
                        stream = getClass().getClassLoader().getResourceAsStream("/" + path);
                    }
                }

                if (stream != null) {
                    // Test if stream has content
                    if (stream.available() > 0) {
                        log.info("✅ Logo found and loaded from: {}", path);
                        // Return a fresh stream
                        return getFreshLogoStream(path);
                    } else {
                        stream.close();
                    }
                }
            } catch (Exception e) {
                log.debug("❌ Failed to load logo from: {}", path);
            }
        }

        log.error("❌ Logo not found in any location");
        return null;
    }

    // ✅ Get fresh logo stream without consuming it
    private InputStream getFreshLogoStream(String path) {
        try {
            if (path.startsWith("classpath:")) {
                String resourcePath = path.substring("classpath:".length());
                return getClass().getClassLoader().getResourceAsStream(resourcePath);
            } else if (path.startsWith("src/")) {
                return new FileInputStream(new File(path));
            } else {
                return getClass().getClassLoader().getResourceAsStream(path);
            }
        } catch (Exception e) {
            log.error("Error getting fresh logo stream", e);
            return null;
        }
    }

    // ✅ Create a placeholder logo if real logo is not found
    private InputStream createPlaceholderLogo() {
        try {
            // Create a simple placeholder image
            BufferedImage placeholder = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = placeholder.createGraphics();

            // Set background
            g2d.setColor(new java.awt.Color(44, 62, 80)); // Dark blue
            g2d.fillRect(0, 0, 200, 200);

            // Draw border
            g2d.setColor(java.awt.Color.WHITE);
            g2d.drawRect(0, 0, 199, 199);

            // Draw text
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            g2d.drawString("SGH", 70, 100);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            g2d.drawString("LOGO", 75, 120);

            g2d.dispose();

            // Convert to InputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(placeholder, "PNG", baos);
            return new ByteArrayInputStream(baos.toByteArray());

        } catch (Exception e) {
            log.error("Error creating placeholder logo", e);
            return null;
        }
    }

    // ✅ DEBUG METHOD TO CHECK RESOURCES
    private void debugResources() {
        try {
            log.info("=== RESOURCE DEBUG INFORMATION ===");

            // Check classpath root
            java.net.URL root = getClass().getClassLoader().getResource("");
            log.info("🔍 Classpath root: {}", root);

            // Check image directory
            java.net.URL imageDir = getClass().getClassLoader().getResource("image/");
            log.info("🔍 Image directory URL: {}", imageDir);

            if (imageDir != null) {
                File dir = new File(imageDir.getFile());
                if (dir.exists() && dir.isDirectory()) {
                    String[] files = dir.list();
                    log.info("📁 Files in image directory: {}", Arrays.toString(files));
                }
            }

            // Try specific paths
            String[] testPaths = {
                    "image/SGH-logo.png",
                    "/image/SGH-logo.png",
                    "static/image/SGH-logo.png",
                    "/static/image/SGH-logo.png"
            };

            for (String path : testPaths) {
                InputStream testStream = getClass().getClassLoader().getResourceAsStream(path);
                if (testStream != null) {
                    log.info("✅ Found resource: {}", path);
                    testStream.close();
                } else {
                    log.info("❌ Not found: {}", path);
                }
            }

            // Check file system
            File fileSystemLogo = new File("src/main/resources/image/SGH-logo.png");
            log.info("🔍 File system logo exists: {}", fileSystemLogo.exists());
            if (fileSystemLogo.exists()) {
                log.info("🔍 File system logo size: {} bytes", fileSystemLogo.length());
            }

            log.info("=== END RESOURCE DEBUG ===");
        } catch (Exception e) {
            log.error("Error during resource debugging", e);
        }
    }

    @Override
    public byte[] generateSpaceAvailabilityReport(Map<String, Object> parameters, String format) throws JRException {
        Connection connection = null;
        InputStream logoStream = null;
        try {
            connection = dataSource.getConnection();
            JasperReport jasperReport = reportCompiler.getCompiledReport("space_availability_report");

            // Load logo for this report too
            logoStream = loadLogoWithMultipleMethods();
            if (logoStream != null) {
                parameters.put("COMPANY_LOGO", logoStream);
            }

            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "SPACE AVAILABILITY REPORT");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            return exportReportToFormat(jasperPrint, format);
        } catch (Exception e) {
            log.error("Error generating space availability report", e);
            throw new JRException("Failed to generate space availability report: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStream(logoStream);
        }
    }

    @Override
    public byte[] generateSpaceByFloorReport(Integer floorId, Map<String, Object> parameters, String format) throws JRException {
        Connection connection = null;
        InputStream logoStream = null;
        try {
            connection = dataSource.getConnection();
            JasperReport jasperReport = reportCompiler.getCompiledReport("space_floor_report");

            // Load logo for this report too
            logoStream = loadLogoWithMultipleMethods();
            if (logoStream != null) {
                parameters.put("COMPANY_LOGO", logoStream);
            }

            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "FLOOR SPACE DISTRIBUTION");
            parameters.put("FLOOR_ID", floorId);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            return exportReportToFormat(jasperPrint, format);
        } catch (Exception e) {
            log.error("Error generating space by floor report for floorId: {}", floorId, e);
            throw new JRException("Failed to generate space by floor report: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStream(logoStream);
        }
    }

    private byte[] exportReportToFormat(JasperPrint jasperPrint, String format) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        switch (format.toLowerCase()) {
            case "pdf":
                JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
                break;

            case "excel":
            case "xls":
                JRXlsExporter xlsExporter = getJrXlsExporter(jasperPrint, outputStream);

                xlsExporter.exportReport();
                break;

            case "xlsx":
                JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
                xlsxConfig.setOnePagePerSheet(false);
                xlsxConfig.setRemoveEmptySpaceBetweenRows(true);
                xlsxConfig.setDetectCellType(true);
                xlsxConfig.setWhitePageBackground(false);
                xlsxExporter.setConfiguration(xlsxConfig);

                xlsxExporter.exportReport();
                break;

            case "word":
            case "doc":
            case "docx":
                JRDocxExporter docxExporter = new JRDocxExporter();
                docxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                docxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                SimpleDocxReportConfiguration docxConfig = new SimpleDocxReportConfiguration();
                docxExporter.setConfiguration(docxConfig);

                docxExporter.exportReport();
                break;
            default:
                throw new JRException("Unsupported export format: " + format);
        }

        return outputStream.toByteArray();
    }

    private static JRXlsExporter getJrXlsExporter(JasperPrint jasperPrint, ByteArrayOutputStream outputStream) {
        JRXlsExporter xlsExporter = new JRXlsExporter();
        xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        SimpleXlsReportConfiguration xlsConfig = new SimpleXlsReportConfiguration();
        xlsConfig.setOnePagePerSheet(false);
        xlsConfig.setRemoveEmptySpaceBetweenRows(true);
        xlsConfig.setDetectCellType(true);
        xlsConfig.setWhitePageBackground(false);
        xlsExporter.setConfiguration(xlsConfig);
        return xlsExporter;
    }
}