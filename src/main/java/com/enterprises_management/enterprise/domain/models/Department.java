package com.enterprises_management.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    private Long id;
    private String name;
    private Country country;
    private List<City> cities;
}
