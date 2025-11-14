package com.spring.DTO.response;


import lombok.Data;

@Data
public class FloorResponseDTO {
    private Integer floorId;
    private String level;
    private long branchBranchId;
}