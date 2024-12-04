package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.TaxPayerTypeResponseDto;

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
public class TaxPayerTypeResponse {

    private Long id;
    private String name;

    private List<TaxPayerTypeResponseDto> taxPayerTypes;
}
