package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class LocationEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String address;

    @ManyToOne
    CityEntity city;

    @ManyToOne
    CountryEntity country;

    @ManyToOne
    DepartmentEntity department;
}
