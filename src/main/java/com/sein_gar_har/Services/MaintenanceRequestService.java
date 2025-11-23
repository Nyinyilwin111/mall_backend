package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.MaintenanceRequestDTO;
import com.sein_gar_har.dto.response.MaintenanceRequestResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestService {

    MaintenanceRequestResponse createRequest(MaintenanceRequestDTO requestDTO);

    List<MaintenanceRequestResponse> getAllRequests();

    MaintenanceRequestResponse getRequestById(UUID requestId);

    List<MaintenanceRequestResponse> getRequestsByTenant(UUID tenantId);

    MaintenanceRequestResponse updateRequest(UUID requestId, MaintenanceRequestDTO requestDTO);

    MaintenanceRequestResponse updateStatus(UUID requestId, String status);

    // ADD THIS METHOD
    void deleteRequest(UUID requestId);
}