package com.enterprises_management.enterprise.domain.models;
import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Modelo de dominio que representa la información de una empresa para exportación.
 * Contiene toda la información relevante de una empresa que se necesita para su uso externo, *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class EnterpriseExport {
    
    private UUID id;
    private String idUser;
    private Enterprise enterprise;
}

