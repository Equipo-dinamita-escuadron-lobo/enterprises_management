package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request;

import java.util.List;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.LocationDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.PersonTypeDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EnterpriseCreateRequest {

    //El id no es necesario ya que es autogenerado
    @JsonIgnore
    private Long id;

    @NotBlank(message = "El nombre de la empresa es requerido")
    private String name;

    //@IUniqueNit
    @NotBlank(message = "El NIT es requerido")
    private String nit;

    private String DV;

    @NotBlank(message = "El teléfono es requerido")
    private String phone;

    private String branch; 
 
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email no es válido")
    private String email;

    private String logo;

    List<Long> taxLiabilities;

    @Builder.Default //ACTIVE, INACTIVE, SUSPENDED
    private StateEnum state = StateEnum.ACTIVE;

    @NotNull(message = "El tipo de contribuyente es requerido")
    @Min(value = 1, message = "El tipo de contribuyente no es válido")
    Long taxPayerType;

    @NotNull(message = "El tipo de empresa es requerido")
    @Min(value = 1, message = "El tipo de contribuyente no es válido")
    Long enterpriseType; 

    @NotNull(message = "El tipo de persona es requerido")
    PersonTypeDto personType;

    @NotNull(message = "La ubicación es requerida")
    LocationDto location;
}
