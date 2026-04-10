package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request;

import java.util.List;
import java.util.UUID;

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

/**
 * DTO que representa la solicitud de creación de una empresa.
 * Contiene todos los campos necesarios para crear una nueva empresa en el
 * sistema,
 * incluyendo validaciones para asegurar la integridad de los datos.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCreateRequest {

    /**
     * Identificador autogenerado de la empresa.
     */
    @JsonIgnore
    private Long id;

    /**
     * Nombre o razón social de la empresa.
     */
    @NotBlank(message = "El nombre de la empresa es requerido")
    private String name;

    /**
     * Número de identificación tributaria.
     */
    @NotBlank(message = "El NIT es requerido")
    private String nit;

    /**
     * Dígito de verificación del NIT.
     */
    @NotBlank(message = "El Digito de Identificación es requerido")
    private String DV;

    /**
     * Número de teléfono de contacto.
     */
    @NotBlank(message = "El teléfono es requerido")
    private String phone;

    /**
     * Sector o rama de actividad de la empresa.
     */
    @NotBlank(message = "El sector es requerido")
    private String branch;

    /**
     * Correo electrónico de contacto.
     */
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email no es válido")
    private String email;

    /**
     * URL o ruta del logo de la empresa.
     */
    private String logo;

    /**
     * Código de la actividad principal.
     */
    @NotNull(message = "El código de la actividad principal es requerido")
    @Min(value = 1, message = "El código de la actividad principal no es válido")
    private Long mainActivity;

    @NotNull(message = "El código de la actividad secundaria es requerido")
    @Min(value = 1, message = "El código de la actividad secundaria no es válido")
    private Long secondaryActivity;

    /**
     * Lista de responsabilidades fiscales.
     */
    List<Long> taxLiabilities;

    /**
     * Estado de la empresa (ACTIVE, INACTIVE, SUSPENDED).
     */
    @Builder.Default
    private StateEnum state = StateEnum.ACTIVE;

    /**
     * Tipo de contribuyente.
     */
    @NotNull(message = "El tipo de contribuyente es requerido")
    @Min(value = 1, message = "El tipo de contribuyente no es válido")
    Long taxPayerType;

    /**
     * Tipo de empresa.
     */
    @NotNull(message = "El tipo de empresa es requerido")
    @Min(value = 1, message = "El tipo de contribuyente no es válido")
    Long enterpriseType;

    /**
     * Tipo de persona (natural o jurídica).
     */
    @NotNull(message = "El tipo de persona es requerido")
    PersonTypeDto personType;

    /**
     * Ubicación de la empresa.
     */
    @NotNull(message = "La ubicación es requerida")
    LocationDto location;

    /**
     * Lista de materias disponibles en la empresa.
     */
    List<UUID> subjects;

    /**
     * Lista de métodos de inventario disponibles en la empresa.
     */
    private String inventoryMethods;
}
