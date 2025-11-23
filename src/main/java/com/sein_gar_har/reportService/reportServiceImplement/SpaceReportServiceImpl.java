package com.sein_gar_har.reportService.reportServiceImplement;

import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.dto.reports.SpaceReportDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.reportService.SpaceReportService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.apache.commons.io.FilenameUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
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

    // Supported image formats by JasperReports
    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // Formats that need conversion to be displayed
    private static final Set<String> CONVERSION_FORMATS = Set.of(
            "avif", "heic", "heif", "tiff", "tif", "raw", "cr2", "nef", "arw"
    );

    @Override
    public byte[] generateSpaceDetailReport(String spaceCode, Map<String, Object> parameters) throws JRException {
        InputStream reportStream = null;
        try {
            // Get space data
            SpaceReportDTO spaceDetail = getSpaceDetailData(spaceCode);
            if (spaceDetail == null) {
                throw new JRException("Space not found with code: " + spaceCode);
            }

            // Prepare data list for JasperReports
            List<SpaceReportDTO> dataList = Collections.singletonList(spaceDetail);

            // Load and compile report template
            log.info("Loading and compiling JRXML template for space detail report...");
            reportStream = new ClassPathResource("reports/spaces/space_detail_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Add parameters
            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "SPACE DETAIL REPORT");
            parameters.put("spaceCode", spaceCode);
            parameters.put("HAS_IMAGES", spaceDetail.getImageUrls() != null && !spaceDetail.getImageUrls().isEmpty());
            parameters.put("TOTAL_IMAGES", spaceDetail.getImageCount());
            parameters.put("IMAGE_GALLERY_TEXT", generateImageGalleryText(spaceDetail.getImageCount()));

            // Use Bean Collection Data Source
            JRDataSource jrDataSource = new JRBeanCollectionDataSource(dataList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrDataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            log.error("Error generating space detail report for spaceCode: {}", spaceCode, e);
            throw new JRException("Failed to generate space detail report: " + e.getMessage(), e);
        } finally {
            closeStream(reportStream);
        }
    }

    private SpaceReportDTO convertToPremiumReportDTO(Space space) {
        // Extract and process image URLs with format validation and conversion
        List<String> imageUrls = processImageUrls(space.getImages());

        String description = generateSpaceDescription(space);
        String specialFeatures = generateSpecialFeatures(space);

        // Parse amenities from JSON and format for display
        List<String> amenityList = parseAmenitiesFromJson(space.getAmenities());
        String formattedAmenities = formatAmenitiesForDisplay(amenityList);

        return SpaceReportDTO.builder()
                .spaceId(space.getSpaceId() != null ? space.getSpaceId().toString() : "N/A")
                .spaceCode(space.getSpaceCode())
                .location(space.getLocation())
                .sizeSqft(space.getSizeSqft())
                .price(space.getPrice() != null ? BigDecimal.valueOf(space.getPrice()) : BigDecimal.ZERO)
                .amenities(formattedAmenities) // Formatted amenities string
                .amenityList(amenityList) // List of amenities for badge display
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
                .contactPhone(" 09798751111 | 09784425961 ")
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

    /**
     * Format amenities for display in report
     */
    private String formatAmenitiesForDisplay(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "Standard amenities included";
        }
        return String.join(", ", amenities);
    }

    /**
     * Process image URLs to handle various formats including conversion
     */
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

    /**
     * Process image URL with format detection and conversion
     */
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

    /**
     * Convert any image format to JPEG base64
     */
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

    /**
     * Convert BufferedImage to base64 JPEG
     */
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

    /**
     * Create a placeholder image for failed conversions
     */
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

    /**
     * Download image bytes from URL
     */
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

    /**
     * Get file extension from URL
     */
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

    /**
     * Check if image is accessible
     */
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

    /**
     * Test HTTP image accessibility
     */
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

        switch (status) {
            case VACANT: return "Immediately Available";
            case OCCUPIED: return "Currently Occupied";
            case MAINTENANCE: return "Under Renovation";
            case RESERVED: return "Reserved - Available Soon";
            default: return "Check Availability";
        }
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
        parameters.putIfAbsent("COMPANY_NAME", "Sein Gar Har");
        parameters.putIfAbsent("COMPANY_SLOGAN", "Quality Properties, Exceptional Service");
        parameters.putIfAbsent("COMPANY_LOGO", "classpath:reports/images/SGH-logo.png");
        parameters.putIfAbsent("REPORT_DATE", new Date());
        parameters.putIfAbsent("GENERATED_BY", "Report System");
        parameters.putIfAbsent("REPORT_TIMEZONE", "Asia/Yangon");
        parameters.putIfAbsent("WATERMARK_TEXT", "CONFIDENTIAL");
        parameters.putIfAbsent("CONTACT_PERSON", "Sein Gar Har Property Manager");
        parameters.putIfAbsent("CONTACT_EMAIL", "info@seingarhar.com");
        parameters.putIfAbsent("CONTACT_PHONE", "09798751111 | 09784425961");
        parameters.putIfAbsent("WEBSITE", "www.seingarhar.com");
        parameters.putIfAbsent("REPORT_VERSION", "1.0");
        parameters.putIfAbsent("REPORT_STYLE", "STANDARD");
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

        public String getProcessedUrl() { return processedUrl; }
        public boolean isConverted() { return converted; }
        public boolean isSuccess() { return success; }
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
    public byte[] generateSpaceListReport(Map<String, Object> parameters) throws JRException {
        Connection connection = null;
        InputStream reportStream = null;
        try {
            connection = dataSource.getConnection();
            reportStream = new ClassPathResource("reports/spaces/space_list_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            addPremiumParameters(parameters);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("Error generating space list report", e);
            throw new JRException("Failed to generate space list report: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStream(reportStream);
        }
    }

    @Override
    public byte[] generateSpaceAvailabilityReport(Map<String, Object> parameters) throws JRException {
        Connection connection = null;
        InputStream reportStream = null;
        try {
            connection = dataSource.getConnection();
            reportStream = new ClassPathResource("reports/spaces/space_availability_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "SPACE AVAILABILITY REPORT");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("Error generating space availability report", e);
            throw new JRException("Failed to generate space availability report: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStream(reportStream);
        }
    }

    @Override
    public byte[] generateSpaceByFloorReport(Integer floorId, Map<String, Object> parameters) throws JRException {
        Connection connection = null;
        InputStream reportStream = null;
        try {
            connection = dataSource.getConnection();
            reportStream = new ClassPathResource("reports/spaces/space_floor_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            addPremiumParameters(parameters);
            parameters.put("REPORT_TITLE", "FLOOR SPACE DISTRIBUTION");
            parameters.put("FLOOR_ID", floorId);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("Error generating space by floor report for floorId: {}", floorId, e);
            throw new JRException("Failed to generate space by floor report: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
            closeStream(reportStream);
        }
    }
}