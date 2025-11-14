//package com.spring.Services.ServiceImplements;
//
//import com.spring.DTO.request.LeaseRequest;
//import com.spring.DTO.response.LeaseResponse;
//import com.spring.Entity.Lease;
//import com.spring.Entity.Space;
//import com.spring.Entity.User;
//import com.spring.Repository.LeaseRepository;
//import com.spring.Repository.SpaceRepository;
//import com.spring.Repository.UserRepository;
//import com.spring.Services.LeaseService;
//import com.spring.Services.S3Service;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class LeaseServiceImpl implements LeaseService {
//
//    @Autowired
//    private LeaseRepository leaseRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private SpaceRepository spaceRepository;
//
//    @Autowired
//    private S3Service s3Service;
//
//    @Override
//    @Transactional
//    public LeaseResponse createLease(LeaseRequest leaseRequest) {
//        User tenant = userRepository.findById(leaseRequest.getTenantId())
//                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + leaseRequest.getTenantId()));
//
//        Space space = spaceRepository.findById(leaseRequest.getSpaceId())
//                .orElseThrow(() -> new RuntimeException("Space not found with id: " + leaseRequest.getSpaceId()));
//
//        Lease lease = new Lease();
//        lease.setTenant(tenant);
//        lease.setSpace(space);
//        lease.setStartDate(leaseRequest.getStartDate());
//        lease.setEndDate(leaseRequest.getEndDate());
//        lease.setRentAmount(leaseRequest.getRentAmount());
//        lease.setDepositAmount(leaseRequest.getDepositAmount() != null ? 
//                leaseRequest.getDepositAmount() : java.math.BigDecimal.ZERO);
//
//        // Set status
//        if (leaseRequest.getStatus() != null && !leaseRequest.getStatus().isEmpty()) {
//            try {
//                lease.setStatus(Lease.LeaseStatus.valueOf(leaseRequest.getStatus().toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                throw new RuntimeException("Invalid lease status: " + leaseRequest.getStatus());
//            }
//        } else {
//            lease.setStatus(Lease.LeaseStatus.DRAFT);
//        }
//
//        // Upload contract file
//        if (leaseRequest.getContractFile() != null && !leaseRequest.getContractFile().isEmpty()) {
//            String contractUrl = s3Service.uploadFile(leaseRequest.getContractFile());
//            lease.setContractDocUrl(contractUrl);
//        }
//
//        Lease savedLease = leaseRepository.save(lease);
//        return new LeaseResponse(savedLease);
//    }
//
//    @Override
//    public List<LeaseResponse> getAllLeases() {
//        return leaseRepository.findAll()
//                .stream()
//                .map(LeaseResponse::new)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public LeaseResponse getLeaseById(Long id) {
//        Lease lease = leaseRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + id));
//        return new LeaseResponse(lease);
//    }
//
//    @Override
//    @Transactional
//    public LeaseResponse updateLease(Long id, LeaseRequest leaseRequest) {
//        Lease existingLease = leaseRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + id));
//
//        // Update tenant if provided
//        if (leaseRequest.getTenantId() != null) {
//            User tenant = userRepository.findById(leaseRequest.getTenantId())
//                    .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + leaseRequest.getTenantId()));
//            existingLease.setTenant(tenant);
//        }
//
//        // Update space if provided
//        if (leaseRequest.getSpaceId() != null) {
//            Space space = spaceRepository.findById(leaseRequest.getSpaceId())
//                    .orElseThrow(() -> new RuntimeException("Space not found with id: " + leaseRequest.getSpaceId()));
//            existingLease.setSpace(space);
//        }
//
//        // Update other fields
//        if (leaseRequest.getStartDate() != null) {
//            existingLease.setStartDate(leaseRequest.getStartDate());
//        }
//        if (leaseRequest.getEndDate() != null) {
//            existingLease.setEndDate(leaseRequest.getEndDate());
//        }
//        if (leaseRequest.getRentAmount() != null) {
//            existingLease.setRentAmount(leaseRequest.getRentAmount());
//        }
//        if (leaseRequest.getDepositAmount() != null) {
//            existingLease.setDepositAmount(leaseRequest.getDepositAmount());
//        }
//
//        // Update status
//        if (leaseRequest.getStatus() != null && !leaseRequest.getStatus().isEmpty()) {
//            try {
//                existingLease.setStatus(Lease.LeaseStatus.valueOf(leaseRequest.getStatus().toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                throw new RuntimeException("Invalid lease status: " + leaseRequest.getStatus());
//            }
//        }
//
//        // Handle contract file update
//        if (leaseRequest.getContractFile() != null && !leaseRequest.getContractFile().isEmpty()) {
//            // Delete old contract file from S3 if exists
//            if (existingLease.getContractDocUrl() != null && !existingLease.getContractDocUrl().isEmpty()) {
//                s3Service.deleteFile(existingLease.getContractDocUrl());
//            }
//            // Upload new contract file
//            String newContractUrl = s3Service.uploadFile(leaseRequest.getContractFile());
//            existingLease.setContractDocUrl(newContractUrl);
//        }
//
//        existingLease.preUpdate();
//        Lease updatedLease = leaseRepository.save(existingLease);
//        return new LeaseResponse(updatedLease);
//    }
//
//    @Override
//    @Transactional
//    public boolean deleteLease(Long id) {
//        Optional<Lease> leaseOptional = leaseRepository.findById(id);
//        if (leaseOptional.isPresent()) {
//            Lease lease = leaseOptional.get();
//            
//            // Delete contract file from S3 if exists
//            if (lease.getContractDocUrl() != null && !lease.getContractDocUrl().isEmpty()) {
//                s3Service.deleteFile(lease.getContractDocUrl());
//            }
//            
//            leaseRepository.deleteById(id);
//            return true;
//        }
//        return false;
//    }
//}
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
import com.spring.Services.LocalStorageService;
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
    private LeaseRepository leaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private LocalStorageService localStorageService;

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
        return new LeaseResponse(updatedLease);
    }

    @Override
    @Transactional
    public boolean deleteLease(Long id) {
        Optional<Lease> leaseOptional = leaseRepository.findById(id);
        if (leaseOptional.isPresent()) {
            Lease lease = leaseOptional.get();
            
            // Delete contract file from local storage if exists
            if (lease.getContractDocUrl() != null && !lease.getContractDocUrl().isEmpty()) {
                localStorageService.deleteFile(lease.getContractDocUrl());
            }
            
            leaseRepository.deleteById(id);
            return true;
        }
        return false;
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
    // Add this method to your LeaseServiceImpl
    // In LeaseServiceImpl.java - FIX THIS METHOD
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
                user = userRepository.findByFullName(username);
                if (user == null) {
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

    // In LeaseServiceImpl.java - Fix this method
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
    // In LeaseServiceImpl.java - Update to use Long
    // Alternative implementation in LeaseServiceImpl
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

}
