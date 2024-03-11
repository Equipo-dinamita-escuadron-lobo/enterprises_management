package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="location")
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
