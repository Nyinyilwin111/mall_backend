package com.sein_gar_har.dto.response;

import com.sein_gar_har.entity.Lease;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class LeaseResponse {
    private Long leaseId;
    private UUID tenantId;
    private String tenantName;
    private UUID spaceId;
    private String spaceName;
    private String spaceLocation;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String contractDocUrl;
    private String companyName;
    private String tenantType;
    private String tenantTrade;
    private String nrc;
    private String address;
    private String heir;

    public LeaseResponse(Lease lease) {
        this.leaseId = lease.getLeaseId();
        this.tenantId = lease.getTenant().getId();
        this.tenantName = lease.getTenant().getFullName();
        this.spaceId = lease.getSpace().getSpaceId();
        this.spaceName = "Space " + lease.getSpace().getLocation();
        this.spaceLocation = lease.getSpace().getLocation();
        this.startDate = lease.getStartDate();
        this.endDate = lease.getEndDate();
        this.rentAmount = lease.getRentAmount();
        this.depositAmount = lease.getDepositAmount();
        this.status = lease.getStatus().toString();
        this.contractDocUrl = lease.getContractDocUrl();
        this.companyName = lease.getCompanyName();
        this.tenantType = lease.getTenantType();
        this.tenantTrade = lease.getTenantTrade();
        this.nrc = lease.getNrc();
        this.address = lease.getAddress();
        this.heir = lease.getHeir();
    }

    // Default constructor for frameworks
    public LeaseResponse() {
    }

}