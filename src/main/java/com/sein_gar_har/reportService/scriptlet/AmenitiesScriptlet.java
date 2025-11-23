//package com.sein_gar_har.reportService.scriptlet;
//
//import net.sf.jasperreports.engine.JRDefaultScriptlet;
//import net.sf.jasperreports.engine.JRScriptletException;
//import java.util.*;
//
//public class AmenitiesScriptlet extends JRDefaultScriptlet {
//
//    private static final String[] BADGE_COLORS = {
//            "#3498DB", "#E74C3C", "#9B59B6", "#F39C12",
//            "#1ABC9C", "#D35400", "#16A085", "#8E44AD"
//    };
//
//    public String formatAmenitiesAsBadges(String amenities) throws JRScriptletException {
//        try {
//            if (amenities == null || amenities.trim().isEmpty()) {
//                return "<style forecolor='#95A5A6' isItalic='true'>No amenities specified</style>";
//            }
//
//            String amenitiesStr = amenities.trim();
//            List<String> amenitiesList = parseAmenities(amenitiesStr);
//
//            if (amenitiesList.isEmpty()) {
//                return "<style forecolor='#95A5A6' isItalic='true'>No amenities specified</style>";
//            }
//
//            StringBuilder styledText = new StringBuilder();
//            int colorIndex = 0;
//
//            for (String amenity : amenitiesList) {
//                if (amenity != null && !amenity.trim().isEmpty()) {
//                    String color = BADGE_COLORS[colorIndex % BADGE_COLORS.length];
//                    styledText.append("<style backcolor='")
//                            .append(color)
//                            .append("' forecolor='#FFFFFF' isBold='true' size='8'> ")
//                            .append(escapeHtml(amenity.trim()))
//                            .append(" </style>  ");
//                    colorIndex++;
//                }
//            }
//
//            return styledText.toString();
//
//        } catch (Exception e) {
//            // Fallback to simple display
//            return "<style forecolor='#2C3E50' size='8'>" + escapeHtml(amenities) + "</style>";
//        }
//    }
//
//    private List<String> parseAmenities(String amenitiesStr) {
//        List<String> result = new ArrayList<>();
//
//        // Try to parse as JSON array
//        if (amenitiesStr.startsWith("[") && amenitiesStr.endsWith("]")) {
//            try {
//                // Simple JSON array parsing without Gson dependency
//                String content = amenitiesStr.substring(1, amenitiesStr.length() - 1);
//                String[] items = content.split("\\s*,\\s*");
//                for (String item : items) {
//                    String cleaned = item.replaceAll("^\"|\"$", "").trim();
//                    if (!cleaned.isEmpty()) {
//                        result.add(cleaned);
//                    }
//                }
//            } catch (Exception e) {
//                // If JSON parsing fails, fall back to comma-separated
//                result = parseCommaSeparated(amenitiesStr);
//            }
//        } else {
//            // Treat as comma-separated string
//            result = parseCommaSeparated(amenitiesStr);
//        }
//
//        return result;
//    }
//
//    private List<String> parseCommaSeparated(String amenitiesStr) {
//        List<String> result = new ArrayList<>();
//        String[] parts = amenitiesStr.split("\\s*,\\s*");
//        for (String part : parts) {
//            if (part != null && !part.trim().isEmpty()) {
//                result.add(part.trim());
//            }
//        }
//        return result;
//    }
//
//    private String escapeHtml(String text) {
//        if (text == null) return "";
//        return text.replace("&", "&amp;")
//                .replace("<", "&lt;")
//                .replace(">", "&gt;")
//                .replace("\"", "&quot;")
//                .replace("'", "&#39;");
//    }
//}