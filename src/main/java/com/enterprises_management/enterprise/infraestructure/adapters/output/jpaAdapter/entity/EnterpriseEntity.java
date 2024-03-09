package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class EnterpriseEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String nit;

    private String DV;

    private String phone;

    private String branch; //sucursal

    private String email;

    private String logo;

    @ManyToMany
    List<TaxLiabilityEntity> taxLiabilities; //reponsabilidades tributarias

    @ManyToOne
    TaxPayerTypeEntity taxPayerType; //tipo de contribuyente


    @ManyToOne
    EnterpriseTypeEntity enterpriseType; //tipo de empresa

    @ManyToOne
    PersonTypeEntity personType; //tipo de persona

    @OneToOne
    LocationEntity location;
    //private Integer tenatId;


}
