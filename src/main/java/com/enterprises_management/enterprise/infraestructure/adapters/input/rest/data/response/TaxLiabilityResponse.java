package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxLiabilityResponse {

    private Long id;
    private String name;
}
