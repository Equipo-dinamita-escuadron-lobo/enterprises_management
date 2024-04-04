package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="tax_liability")
public class TaxLiabilityEntity { // Responsabilidad tributaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
