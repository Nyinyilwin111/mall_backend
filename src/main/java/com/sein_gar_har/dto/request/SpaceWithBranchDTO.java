package com.sein_gar_har.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SpaceWithBranchDTO {
    private UUID spaceId;
    private String spaceCode;
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String branchPhone;

    // Constructor
    public SpaceWithBranchDTO(UUID spaceId, String spaceCode, Long branchId,
                              String branchName, String branchAddress, String branchPhone) {
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.branchPhone = branchPhone;
    }
}