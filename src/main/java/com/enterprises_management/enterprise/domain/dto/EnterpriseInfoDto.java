package com.enterprises_management.enterprise.domain.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseInfoDto {
    
    private UUID id;
    private String name;
    private String nit;
    private String logo;
}
