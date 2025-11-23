package com.sein_gar_har.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceReportDTO {
    private String spaceId;
    private String spaceCode;
    private String location;
    private Double sizeSqft;
    private BigDecimal price;
    private String amenities;
    private String status;
    private String spaceType;
    private String floorLevel;
    private String branchName;
    private LocalDateTime createdAt;
    private Integer imageCount;
    private List<String> imageUrls;
    private List<String> amenityList; // New: Parsed amenities list
    private String description;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String availabilityStatus;
    private String specialFeatures;

    // Premium formatted fields
    public String getCreatedAtFormatted() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
            return createdAt.format(formatter);
        }
        return "Not Available";
    }

    public String getCreatedDate() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            return createdAt.format(formatter);
        }
        return "Not Available";
    }

    public String getPriceFormatted() {
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            return "$" + String.format("%,.2f", price) + " / month";
        }
        return "Price on Request";
    }

    public String getSizeFormatted() {
        if (sizeSqft != null) {
            return String.format("%,.0f", sizeSqft) + " sq.ft.";
        }
        return "Size Not Specified";
    }

    public String getStatusBadge() {
        if (status != null) {
            switch (status.toUpperCase()) {
                case "VACANT": return "🟢 Available";
                case "OCCUPIED": return "🔴 Occupied";
                case "MAINTENANCE": return "🟡 Under Maintenance";
                case "RESERVED": return "🟠 Reserved";
                default: return "⚪ " + status;
            }
        }
        return "⚪ Unknown";
    }

    public String getAmenitiesList() {
        if (amenities != null && !amenities.isEmpty()) {
            // Handle JSON or comma-separated amenities
            String amenitiesText = amenities.replace("\"", "").replace("[", "").replace("]", "");
            return "• " + amenitiesText.replace(",", "\n• ");
        }
        return "• Standard Security\n• Basic Utilities\n• Regular Maintenance";
    }

    public String getImageGalleryInfo() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return "📸 " + imageUrls.size() + " photos available";
        }
        return "📸 No photos available";
    }
}