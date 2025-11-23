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
    private String type;
    private String icon;
    private String badge;
    private String url;
    private String leaseId;
    private String spaceId;
    private String spaceCode;
    private String tenantName;
    private String tenantId;
    private String branchName;
    private String branchID;
    private String createdUserName;
    private String rentAmount;
    private String userToken;
}