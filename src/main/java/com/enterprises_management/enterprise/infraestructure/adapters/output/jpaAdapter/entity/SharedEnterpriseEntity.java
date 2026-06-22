package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "shared_enterprise")
@Getter
@Setter
public class SharedEnterpriseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "enterprise_id", nullable = false)
    private UUID enterpriseId;

    @Column(name = "shared_with_user_id", nullable = false)
    private String sharedWithUserId;
}
