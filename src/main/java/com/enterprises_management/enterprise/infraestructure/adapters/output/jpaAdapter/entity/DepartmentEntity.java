package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import com.enterprises_management.enterprise.domain.models.City;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="department")
public class DepartmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="country_id")
    private CountryEntity country;
    private String name;
    @OneToMany(mappedBy = "department" , cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CityEntity> cities;

}
