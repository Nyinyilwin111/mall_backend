package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.LeaseRepository;
import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.LeaseService;
import com.sein_gar_har.Services.LocalStorageService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.dto.request.LeaseRequest;
import com.sein_gar_har.dto.response.LeaseResponse;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeaseServiceImpl implements LeaseService {

    @Autowired
    LeaseRepository leaseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    LocalStorageService localStorageService;

    @Autowired
    AuditLogService auditLogService;

    @Override
    @Transactional
    public LeaseResponse createLease(LeaseRequest leaseRequest) {
        User tenant = userRepository.findById(leaseRequest.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + leaseRequest.getTenantId()));

        Space space = spaceRepository.findById(leaseRequest.getSpaceId())
                .orElseThrow(() -> new RuntimeException("Space not found with id: " + leaseRequest.getSpaceId()));

        Lease lease = new Lease();
        lease.setTenant(tenant);
        lease.setSpace(space);
        lease.setStartDate(leaseRequest.getStartDate());
        lease.setEndDate(leaseRequest.getEndDate());
        lease.setRentAmount(leaseRequest.getRentAmount());
        lease.setDepositAmount(leaseRequest.getDepositAmount() != null ?
                leaseRequest.getDepositAmount() : java.math.BigDecimal.ZERO);

        // Set new fields
        lease.setCompanyName(leaseRequest.getCompanyName());
        lease.setTenantType(leaseRequest.getTenantType());
        lease.setTenantTrade(leaseRequest.getTenantTrade());
        lease.setNrc(leaseRequest.getNrc());
        lease.setAddress(leaseRequest.getAddress());
        lease.setHeir(leaseRequest.getHeir());

        // Set status
        if (leaseRequest.getStatus() != null && !leaseRequest.getStatus().isEmpty()) {
            try {
                lease.setStatus(Lease.LeaseStatus.valueOf(leaseRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid lease status: " + leaseRequest.getStatus());
            }
        } else {
            lease.setStatus(Lease.LeaseStatus.DRAFT);
        }

        // Upload contract file using LocalStorageService
        if (leaseRequest.getContractFile() != null && !leaseRequest.getContractFile().isEmpty()) {
            String contractUrl = localStorageService.saveFile(leaseRequest.getContractFile());
            lease.setContractDocUrl(contractUrl);
        }

        Lease savedLease = leaseRepository.save(lease);

        // Audit log for lease creation
        try {
            auditLogService.logCreate("Lease", savedLease.getLeaseId().toString(),
                    createLeaseAuditData(savedLease, tenant, space));
        } catch (Exception e) {
            System.err.println("Failed to create audit log for lease creation: " + e.getMessage());
            // Don't throw exception as lease creation was successful
        }

        return new LeaseResponse(savedLease);
    }

    @Override
    public List<LeaseResponse> getAllLeases() {
        return leaseRepository.findAll()
                .stream()
                .map(LeaseResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public LeaseResponse getLeaseById(Long id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + id));
        return new LeaseResponse(lease);
    }

    @Override
    @Transactional
    public LeaseResponse updateLease(Long id, LeaseRequest leaseRequest) {
        Lease existingLease = leaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + id));

        // Store old data for audit log
        Lease oldLeaseData = new Lease();
        copyLeaseData(existingLease, oldLeaseData);

        // Update tenant if provided
        if (leaseRequest.getTenantId() != null) {
            User tenant = userRepository.findById(leaseRequest.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + leaseRequest.getTenantId()));
            existingLease.setTenant(tenant);
        }

        // Update space if provided
        if (leaseRequest.getSpaceId() != null) {
            Space space = spaceRepository.findById(leaseRequest.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Space not found with id: " + leaseRequest.getSpaceId()));
            existingLease.setSpace(space);
        }

        // Update other fields
        if (leaseRequest.getStartDate() != null) {
            existingLease.setStartDate(leaseRequest.getStartDate());
        }
        if (leaseRequest.getEndDate() != null) {
            existingLease.setEndDate(leaseRequest.getEndDate());
        }
        if (leaseRequest.getRentAmount() != null) {
            existingLease.setRentAmount(leaseRequest.getRentAmount());
        }
        if (leaseRequest.getDepositAmount() != null) {
            existingLease.setDepositAmount(leaseRequest.getDepositAmount());
        }

        // Update new fields
        if (leaseRequest.getCompanyName() != null) {
            existingLease.setCompanyName(leaseRequest.getCompanyName());
        }
        if (leaseRequest.getTenantType() != null) {
            existingLease.setTenantType(leaseRequest.getTenantType());
        }
        if (leaseRequest.getTenantTrade() != null) {
            existingLease.setTenantTrade(leaseRequest.getTenantTrade());
        }
        if (leaseRequest.getNrc() != null) {
            existingLease.setNrc(leaseRequest.getNrc());
        }
        if (leaseRequest.getAddress() != null) {
            existingLease.setAddress(leaseRequest.getAddress());
        }
        if (leaseRequest.getHeir() != null) {
            existingLease.setHeir(leaseRequest.getHeir());
        }

        // Update status
        if (leaseRequest.getStatus() != null && !leaseRequest.getStatus().isEmpty()) {
            try {
                existingLease.setStatus(Lease.LeaseStatus.valueOf(leaseRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid lease status: " + leaseRequest.getStatus());
            }
        }

        // Handle contract file update using LocalStorageService
        if (leaseRequest.getContractFile() != null && !leaseRequest.getContractFile().isEmpty()) {
            // Delete old contract file from local storage if exists
            if (existingLease.getContractDocUrl() != null && !existingLease.getContractDocUrl().isEmpty()) {
                localStorageService.deleteFile(existingLease.getContractDocUrl());
            }
            // Upload new contract file
            String newContractUrl = localStorageService.saveFile(leaseRequest.getContractFile());
            existingLease.setContractDocUrl(newContractUrl);
        }

        existingLease.preUpdate();
        Lease updatedLease = leaseRepository.save(existingLease);

        // Audit log for lease update
        try {
            auditLogService.logUpdate("Lease", updatedLease.getLeaseId().toString(),
                    createLeaseAuditData(oldLeaseData, oldLeaseData.getTenant(), oldLeaseData.getSpace()),
                    createLeaseAuditData(updatedLease, updatedLease.getTenant(), updatedLease.getSpace()));
        } catch (Exception e) {
            System.err.println("Failed to create audit log for lease update: " + e.getMessage());
            // Don't throw exception as lease update was successful
        }

        return new LeaseResponse(updatedLease);
    }

    @Override
    @Transactional
    public boolean deleteLease(Long id) {
        Optional<Lease> leaseOptional = leaseRepository.findById(id);
        if (leaseOptional.isPresent()) {
            Lease lease = leaseOptional.get();

            // Store lease data for audit log before deletion
            Lease leaseDataForAudit = new Lease();
            copyLeaseData(lease, leaseDataForAudit);

            // Delete contract file from local storage if exists
            if (lease.getContractDocUrl() != null && !lease.getContractDocUrl().isEmpty()) {
                localStorageService.deleteFile(lease.getContractDocUrl());
            }

            leaseRepository.deleteById(id);

            // Audit log for lease deletion
            try {
                auditLogService.logDelete("Lease", id.toString(),
                        createLeaseAuditData(leaseDataForAudit, leaseDataForAudit.getTenant(), leaseDataForAudit.getSpace()));
            } catch (Exception e) {
                System.err.println("Failed to create audit log for lease deletion: " + e.getMessage());
                // Don't throw exception as lease deletion was successful
            }

            return true;
        }
        return false;
    }

    @Override
    public List<Lease> getLeasesByTenantId(String tenantId) {
        try {
            UUID tenantUUID = UUID.fromString(tenantId);
            return leaseRepository.findAllByTenantId(tenantUUID);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid tenant ID format: " + tenantId);
            return List.of();
        }
    }

    @Override
    public List<LeaseResponse> getLeasesForCurrentUser(Principal principal) {
        try {
            String username = principal.getName();
            System.out.println("🔍 Getting leases for user: " + username);

            // Try to find user by email first
            Optional<User> userOptional = userRepository.findByEmail(username);
            User user;

            if (userOptional.isPresent()) {
                user = userOptional.get();
            } else {
                // Try by fullName as fallback
                Optional<User> userOption = userRepository.findByFullName(username);

                if (userOption.isPresent()) {
                    user = userOption.get();
                } else {
                    throw new RuntimeException("User not found: " + username);
                }
            }

            System.out.println("✅ User found: " + user.getEmail() + ", ID: " + user.getId());

            List<Lease> userLeases = leaseRepository.findAllByTenantId(user.getId());
            System.out.println("📋 Found " + userLeases.size() + " leases for user");

            return userLeases.stream()
                    .map(LeaseResponse::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("💥 Error in getLeasesForCurrentUser:");
            e.printStackTrace();
            throw new RuntimeException("Failed to get user leases: " + e.getMessage());
        }
    }

    @Override
    public LeaseResponse getLeaseWithDetails(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        // Eagerly fetch related entities
        if (lease.getSpace() != null) {
            lease.getSpace().getSpaceType(); // Force loading
            lease.getSpace().getFloor(); // Force loading
        }

        return new LeaseResponse(lease);
    }

    @Override
    public List<Lease> getLeasesByTenantIdAndBranchId(String tenantId, Long branchId) {
        try {
            UUID tenantUUID = UUID.fromString(tenantId);
            List<Lease> leases = leaseRepository.findAllByTenantIdAndBranchId(tenantUUID, branchId);
            System.out.println("📋 Direct repository call found " + leases.size() + " leases for tenant " + tenantId + " in branch " + branchId);
            return leases;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid tenant ID format: " + tenantId);
            return List.of();
        }
    }

    // Enhanced helper method to create audit data for lease
    private Object createLeaseAuditData(Lease lease, User tenant, Space space) {
        java.util.Map<String, Object> auditData = new java.util.HashMap<>();
        auditData.put("leaseId", lease.getLeaseId());
        auditData.put("tenantId", lease.getTenant() != null ? lease.getTenant().getId().toString() : null);
        auditData.put("tenantName", tenant != null ? tenant.getFullName() : null);
        auditData.put("tenantEmail", tenant != null ? tenant.getEmail() : null);
        auditData.put("spaceId", lease.getSpace() != null ? lease.getSpace().getSpaceId().toString() : null);
        auditData.put("spaceName", space != null ? space.getSpaceCode() : null);
        auditData.put("spaceLocation", space != null ? space.getLocation() : null);
        auditData.put("startDate", lease.getStartDate());
        auditData.put("endDate", lease.getEndDate());
        auditData.put("rentAmount", lease.getRentAmount());
        auditData.put("depositAmount", lease.getDepositAmount());
        auditData.put("status", lease.getStatus() != null ? lease.getStatus().name() : null);
        auditData.put("companyName", lease.getCompanyName());
        auditData.put("tenantType", lease.getTenantType());
        auditData.put("tenantTrade", lease.getTenantTrade());
        auditData.put("nrc", lease.getNrc());
        auditData.put("address", lease.getAddress());
        auditData.put("heir", lease.getHeir());
        auditData.put("contractDocUrl", lease.getContractDocUrl());

        // Add descriptive name for audit log display
        String displayName = "Lease #" + lease.getLeaseId();
        if (tenant != null && tenant.getFullName() != null && space != null && space.getSpaceCode() != null) {
            displayName = tenant.getFullName() + " - " + space.getSpaceCode();
        } else if (tenant != null && tenant.getFullName() != null) {
            displayName = tenant.getFullName();
        } else if (space != null && space.getSpaceCode() != null) {
            displayName = space.getSpaceCode();
        }
        auditData.put("displayName", displayName);

        return auditData;
    }

    // Helper method to copy lease data for audit logging
    private void copyLeaseData(Lease source, Lease target) {
        target.setLeaseId(source.getLeaseId());
        target.setTenant(source.getTenant());
        target.setSpace(source.getSpace());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setRentAmount(source.getRentAmount());
        target.setDepositAmount(source.getDepositAmount());
        target.setStatus(source.getStatus());
        target.setCompanyName(source.getCompanyName());
        target.setTenantType(source.getTenantType());
        target.setTenantTrade(source.getTenantTrade());
        target.setNrc(source.getNrc());
        target.setAddress(source.getAddress());
        target.setHeir(source.getHeir());
        target.setContractDocUrl(source.getContractDocUrl());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}