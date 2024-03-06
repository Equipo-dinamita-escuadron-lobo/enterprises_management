package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class PersonTypeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String type;
    private String name;
    private String surname;

    private String bussinessName;//Razon social
}
