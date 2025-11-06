package com.spring.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.spring.Entity.Lease;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaseResponse {
    private Long leaseId;
    private String tenantName;
    private String spaceName;
    private String spaceLocation;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long spaceId;
    private String contractDocUrl;
    
    public LeaseResponse(Lease lease) {
        this.leaseId = lease.getLeaseId();
        this.tenantName = lease.getTenant().getFullName();  // Tenant Entity ထဲက name field သုံး
        this.spaceName = lease.getSpace().getSpaceName(); // Space Entity ထဲက name field သုံး
        this.spaceLocation=lease.getSpace().getLocation();
        this.startDate = lease.getStartDate();
        this.endDate = lease.getEndDate();
        this.rentAmount = lease.getRentAmount();
        this.depositAmount = lease.getDepositAmount();
        this.spaceId=lease.getSpace().getSpaceId();
        this.status = lease.getStatus().toString();
        this.contractDocUrl=lease.getContractDocUrl();
    }

}
