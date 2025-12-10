// AuditLog.java
package com.sein_gar_har.auditEntity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "auditlog")
public class AuditLog {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID logId;

    private String action;
    private String tableAffected;
    private String recordId;

    @Column(columnDefinition = "TEXT")
    private String oldValues;

    @Column(columnDefinition = "TEXT")
    private String newValues;

    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String userFriendlyMessage;

    // Helper methods for reporting
    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.toString() : "N/A";
    }

    public String getActionType() {
        if (action == null) return "UNKNOWN";
        return switch (action.toUpperCase()) {
            case "CREATE" -> "Creation";
            case "UPDATE" -> "Modification";
            case "DELETE" -> "Deletion";
            case "LOGIN" -> "Login Attempt";
            default -> action;
        };
    }

    public boolean isLoginAction() {
        return "LOGIN".equalsIgnoreCase(action);
    }

    public boolean isRoleChange() {
        return "UPDATE".equalsIgnoreCase(action) &&
                ("User".equalsIgnoreCase(tableAffected) ||
                        (newValues != null && newValues.contains("roles")));
    }

    public String getStatusFromLogin() {
        if (!isLoginAction() || newValues == null) return "N/A";
        try {
            if (newValues.contains("\"status\":\"SUCCESS\"")) return "SUCCESS";
            if (newValues.contains("\"status\":\"FAILED\"")) return "FAILED";
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return "UNKNOWN";
    }

    public String getExtractedEmail() {
        if (newValues != null && newValues.contains("\"email\":")) {
            return newValues.replaceAll(".*\"email\":\"([^\"]+)\".*", "$1");
        }
        return "Unknown User";
    }

    public String getLoginStatus() {
        return getStatusFromLogin();
    }

    public String getShortUserAgent() {
        if (userAgent == null) return "";
        if (userAgent.length() > 50) {
            return userAgent.substring(0, 47) + "...";
        }
        return userAgent;
    }
}