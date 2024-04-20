package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.enterprises_management.enterprise.domain.enums.StateEnum;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="enterprise")
public class EnterpriseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String nit;

    private String DV;

    private String phone;

    private String branch; //sucursal

    private String email;

    private String logo;

    private StateEnum state;

    @ManyToMany
    List<TaxLiabilityEntity> taxLiabilities; //reponsabilidades tributarias

    @ManyToOne
    TaxPayerTypeEntity taxPayerType; //tipo de contribuyente

    @ManyToOne
    @JoinColumn(name = "enterprise_type_id", referencedColumnName = "id")
    EnterpriseTypeEntity enterpriseType; //tipo de empresa

    @ManyToOne
    @JoinColumn(name = "person_type_id", referencedColumnName = "id")
    PersonTypeEntity personType; //tipo de persona

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    LocationEntity location;
    //private Integer tenatId;


}
