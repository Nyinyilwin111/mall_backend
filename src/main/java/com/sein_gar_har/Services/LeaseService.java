package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.LeaseRequest;
import com.sein_gar_har.dto.response.LeaseResponse;
import com.sein_gar_har.entity.Lease;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface LeaseService {

    LeaseResponse createLease(LeaseRequest leaseRequest);

    List<LeaseResponse> getAllLeases();

    LeaseResponse getLeaseById(Long id);

    LeaseResponse updateLease(Long id, LeaseRequest leaseRequest);

    boolean deleteLease(Long id);

    List<Lease> getLeasesByTenantId(String tenantId);

    List<LeaseResponse> getLeasesForCurrentUser(Principal principal);

    LeaseResponse getLeaseWithDetails(Long leaseId);

    List<Lease> getLeasesByTenantIdAndBranchId(String tenantId, Long branchId);
}
