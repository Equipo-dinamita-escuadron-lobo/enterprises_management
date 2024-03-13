package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentAddressResponse {
    private Long id;
    private String name;
}
