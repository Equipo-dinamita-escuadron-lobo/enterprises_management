package com.enterprises_management.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonType {

    private Long id;
    private String type;
    private String name;
    private String surname;

    private String bussinessName;//Razon social
}
