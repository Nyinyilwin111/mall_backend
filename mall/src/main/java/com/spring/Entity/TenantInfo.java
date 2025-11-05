package com.spring.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;

import java.util.UUID;
@Entity
@Data
public class TenantInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @Column(name = "info_id", columnDefinition = "BINARY(16)")
    private UUID InfoId;

    @Column(name = "description", columnDefinition = "NVARCHAR(20)")
    private String companyName;

    @Column(name = "description", columnDefinition = "NVARCHAR(20)")
    private String tenantType;

    @Column(name = "description", columnDefinition = "NVARCHAR(20)")
    private String tenantTrade;

    @Column(name = "description", columnDefinition = "NVARCHAR(20)")
    private String NRC;

    @Column(name = "description", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "description", columnDefinition = "NVARCHAR(20)")
    private String heir;
}
