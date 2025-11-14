package com.spring.Services;

import com.spring.DTO.request.LeaseRequest;
import com.spring.DTO.response.LeaseResponse;
import com.spring.Entity.Lease;

import java.security.Principal;
import java.util.List;

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
