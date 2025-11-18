package com.sein_gar_har.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleNotificationRequest {
    private String title;
    private String body;
    private String role;
    private String sentuserId;
}