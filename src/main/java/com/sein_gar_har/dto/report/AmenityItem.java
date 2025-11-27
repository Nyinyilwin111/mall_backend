package com.sein_gar_har.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AmenityItem {
    private String amenity;
    private String icon; // Optional: for icons

    // Constructor for String conversion
    public AmenityItem(String amenityName) {
        this.amenity = amenityName;
        this.icon = getIconForAmenity(amenityName);
    }

    private String getIconForAmenity(String amenity) {
        if (amenity == null) return "•";

        String lowerAmenity = amenity.toLowerCase();
        if (lowerAmenity.contains("air") || lowerAmenity.contains("ac")) return "❄️";
        if (lowerAmenity.contains("park")) return "🅿️";
        if (lowerAmenity.contains("wifi") || lowerAmenity.contains("internet")) return "📶";
        if (lowerAmenity.contains("security")) return "🔒";
        if (lowerAmenity.contains("lift") || lowerAmenity.contains("elevator")) return "🛗";
        return "•";
    }
}