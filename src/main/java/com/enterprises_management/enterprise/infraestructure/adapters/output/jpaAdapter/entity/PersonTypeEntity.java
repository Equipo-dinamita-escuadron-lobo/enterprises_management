package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="person_type")
public class PersonTypeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String type;
    private String name;
    private String surname;

    private String bussinessName;//Razon social
}
