package com.enterprises_management.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    private Long id;
    private String address;
    City city;
    Country country;
    Department department;
}
