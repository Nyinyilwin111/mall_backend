package com.sein_gar_har.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;
@Entity
@Data
public class TenantInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "info_id", columnDefinition = "BINARY(16)")
    private UUID InfoId;

    @Column(name = "company_name", columnDefinition = "NVARCHAR(20)")
    private String companyName;

    @Column(name = "tenant_type", columnDefinition = "NVARCHAR(20)")
    private String tenantType;

    @Column(name = "tenant_trade", columnDefinition = "NVARCHAR(20)")
    private String tenantTrade;

    @Column(name = "NRC", columnDefinition = "NVARCHAR(20)")
    private String NRC;

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "heir", columnDefinition = "NVARCHAR(20)")
    private String heir;
}
