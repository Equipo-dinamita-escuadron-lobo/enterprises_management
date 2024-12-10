package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.TenantId;

import com.enterprises_management.enterprise.domain.enums.StateEnum;

/**
 * Entidad JPA que representa una empresa en la base de datos.
 * Mapea la tabla "enterprise" y define las relaciones con otras entidades.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="enterprise")
public class EnterpriseEntity {

    /**
     * Identificador único de la empresa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Identificador del usuario asociado a la empresa.
     */
    private String idUser;

    /**
     * Nombre o razón social de la empresa.
     */
    private String name;

    /**
     * Número de identificación tributaria.
     */
    private String nit;

    /**
     * Dígito de verificación del NIT.
     */
    private String DV;

    /**
     * Número de teléfono de contacto.
     */
    private String phone;

    /**
     * Sector o rama de actividad de la empresa.
     */
    private String branch;

    /**
     * Correo electrónico de contacto.
     */
    private String email;

    /**
     * URL o ruta del logo de la empresa.
     */
    private String logo;

    /**
     * Estado actual de la empresa.
     */
    private StateEnum state;

    /**
     * Código de la actividad principal.
     */
    private Long mainActivity;

    /**
     * Código de la actividad secundaria.
     */
    private Long secondaryActivity;

    /**
     * Lista de responsabilidades fiscales de la empresa.
     */
    @ManyToMany
    List<TaxLiabilityEntity> taxLiabilities;

    /**
     * Tipo de contribuyente de la empresa.
     */
    @ManyToOne
    TaxPayerTypeEntity taxPayerType;

    /**
     * Tipo de empresa.
     */
    @ManyToOne
    @JoinColumn(name = "enterprise_type_id", referencedColumnName = "id")
    EnterpriseTypeEntity enterpriseType;

    /**
     * Tipo de persona (natural o jurídica).
     */
    @ManyToOne
    @JoinColumn(name = "person_type_id", referencedColumnName = "id")
    PersonTypeEntity personType;

    /**
     * Ubicación de la empresa.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    LocationEntity location;

    /**
     * Identificador del inquilino (tenant) para multi-tenancy.
     */
    @TenantId
    String tenantId;
}
