package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TaxLiabilityEntity { // Responsabilidad tributaria
    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
