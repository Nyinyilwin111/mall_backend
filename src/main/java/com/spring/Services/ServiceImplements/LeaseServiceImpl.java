package com.spring.Services.ServiceImplements;

import com.spring.DTO.request.LeaseRequest;
import com.spring.DTO.response.LeaseResponse;
import com.spring.Entity.Lease;
import com.spring.Entity.Space;
import com.spring.Entity.User;
import com.spring.Repository.LeaseRepository;
import com.spring.Repository.SpaceRepository;
import com.spring.Repository.UserRepository;
import com.spring.Services.LeaseService;
import com.spring.Services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaseServiceImpl implements LeaseService {

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private S3Service s3Service;

    private static final String S3_FOLDER = "lease-contracts";

    @Override
    public Lease createLease(LeaseRequest request) {
        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + request.getTenantId()));

        Space space = spaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new RuntimeException("Space not found with id: " + request.getSpaceId()));

        Lease lease = new Lease();
        lease.setTenant(tenant);
        lease.setSpace(space);
        lease.setStartDate(request.getStartDate());
        lease.setEndDate(request.getEndDate());
        lease.setRentAmount(request.getRentAmount());
        lease.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO);
        lease.setStatus(Lease.LeaseStatus.DRAFT);

        // Upload contract to S3
        if (request.getContractFile() != null && !request.getContractFile().isEmpty()) {
            try {
                String s3Url = s3Service.uploadFile(request.getContractFile(), S3_FOLDER);
                lease.setContractDocUrl(s3Url);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload contract file to S3: " + e.getMessage(), e);
            }
        }

        return leaseRepository.save(lease);
    }

    @Override
    public List<LeaseResponse> getAllLeases() {
        return leaseRepository.findAll().stream().map(lease -> {
            LeaseResponse dto = new LeaseResponse(lease);
            dto.setLeaseId(lease.getLeaseId());
            dto.setTenantName(lease.getTenant().getFullName());
            dto.setSpaceName(lease.getSpace().getSpaceName());
            dto.setSpaceLocation(lease.getSpace().getLocation());
            dto.setStartDate(lease.getStartDate());
            dto.setEndDate(lease.getEndDate());	
            dto.setRentAmount(lease.getRentAmount());
            dto.setDepositAmount(lease.getDepositAmount());
            dto.setStatus(lease.getStatus().name());
            dto.setSpaceId(lease.getSpace().getSpaceId());
            
            dto.setContractDocUrl(lease.getContractDocUrl()); // Add this line
            return dto;
        }).collect(Collectors.toList());
    }

    // NEW: Get lease by ID
    @Override
    public LeaseResponse getLeaseById(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        // ✅ Entity ကို DTO ပြောင်း
        return new LeaseResponse(lease);
    }


    // NEW: Update lease
    @Override
    public LeaseResponse updateLease(Long leaseId, LeaseRequest request) {
        Lease existingLease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        // Update tenant if provided
        if (request.getTenantId() != null) {
            User tenant = userRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + request.getTenantId()));
            existingLease.setTenant(tenant);
        }

        // Update space if provided
        if (request.getSpaceId() != null) {
            Space space = spaceRepository.findById(request.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Space not found with id: " + request.getSpaceId()));
            existingLease.setSpace(space);
        }

        // Update other fields
        if (request.getStartDate() != null) {
            existingLease.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            existingLease.setEndDate(request.getEndDate());
        }
        if (request.getRentAmount() != null) {
            existingLease.setRentAmount(request.getRentAmount());
        }
        if (request.getDepositAmount() != null) {
            existingLease.setDepositAmount(request.getDepositAmount());
        }
         
        // Update status if provided
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                existingLease.setStatus(Lease.LeaseStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid lease status: " + request.getStatus());
            }
        }

        // Handle contract file update
        if (request.getContractFile() != null && !request.getContractFile().isEmpty()) {
            try {
                // Delete old contract file from S3 if exists
                if (existingLease.getContractDocUrl() != null && !existingLease.getContractDocUrl().isEmpty()) {
                    s3Service.deleteFile(existingLease.getContractDocUrl());
                }
                // Upload new contract file
                String s3Url = s3Service.uploadFile(request.getContractFile(), S3_FOLDER);
                existingLease.setContractDocUrl(s3Url);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload contract file to S3: " + e.getMessage(), e);
            }
        }

        // Update timestamp
        existingLease.preUpdate();
        Lease updated = leaseRepository.save(existingLease);

        LeaseResponse dto = new LeaseResponse(updated);
        dto.setLeaseId(updated.getLeaseId());
        dto.setTenantName(updated.getTenant().getFullName());
        dto.setSpaceName(updated.getSpace().getSpaceName());
        dto.setSpaceLocation(updated.getSpace().getLocation());
        dto.setRentAmount(updated.getRentAmount());
        dto.setDepositAmount(updated.getDepositAmount());
        dto.setStartDate(updated.getStartDate());
        dto.setEndDate(updated.getEndDate());
        dto.setStatus(updated.getStatus().name());
        dto.setContractDocUrl(updated.getContractDocUrl());

        return dto; // <<<< return LeaseResponse
    }

   

    // NEW: Delete lease (already exists but adding @Override)
    @Override
    public void deleteLease(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        if (lease.getContractDocUrl() != null && !lease.getContractDocUrl().isEmpty()) {
            try {
                s3Service.deleteFile(lease.getContractDocUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete S3 file: " + e.getMessage());
            }
        }

        leaseRepository.delete(lease);
    }
}