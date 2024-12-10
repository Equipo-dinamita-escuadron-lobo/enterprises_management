package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseCreateMannegerPort;
import com.enterprises_management.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.input.IEnterpriseUpdateManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ILocationMangerPort;
import com.enterprises_management.enterprise.application.ports.input.IPersonTypeManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import com.enterprises_management.enterprise.application.ports.input.PdfRUTContent;
import com.enterprises_management.enterprise.application.ports.output.PdfRUTContentOutput;
import com.enterprises_management.enterprise.application.ports.services.PdfRUTService;
import com.enterprises_management.enterprise.application.ports.input.ITaxPayerTypeManagerPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseByIdResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxPayerTypeResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseCreateRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseSearchRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ITaxLiabilityRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ITaxPayerTypeRestMapper;
import com.enterprises_management.enterprise.infraestructure.security.IJwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Controlador REST para gestionar las operaciones relacionadas con las empresas.
 * Proporciona endpoints para crear, actualizar, y recuperar información de empresas.
 */
@RequestMapping("/api/enterprises")
@RestController
@AllArgsConstructor
@Validated
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('admin_client')")
public class EnterpriseController {

    private final ITaxLiabilityManagerPort taxLiabilityManagerPort;
    private final ITaxLiabilityRestMapper taxLiabilityRestMapper;

    private final ITaxPayerTypeManagerPort taxPayerTypeManagerPort;
    private final ITaxPayerTypeRestMapper taxPayerTypeRestMapper;

    private final IEnterpriseSearchManagerPort enterpriseSearchManagerPort;

    private final IEnterpriseCreateMannegerPort enterpriseCreateMannegerPort;
    private final IEnterpriseCreateRestMapper enterpriseCreateMapper;

    private final ILocationMangerPort locationMangerPort;
    private final IPersonTypeManagerPort personTypeManagerPort;

    private final IEnterpriseUpdateManagerPort enterpriseUpdateManagerPort;
    private final IEnterpriseSearchRestMapper enterpriseSearchMapper;

    private final IJwtUtils jwtUtils;

    /**
     * Obtiene todas las responsabilidades tributarias.
     *
     * @return lista de responsabilidades tributarias
     */
    @GetMapping("/taxliabilities")
    @Operation(summary = "Obtener todas las responsabilidades tributarias", description = "Recupera una lista de todas las responsabilidades tributarias disponibles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Responsabilidades tributarias recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaxLiabilityResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las responsabilidades tributarias", content = @Content)
    })
    public ResponseEntity<List<TaxLiabilityResponse>> getAllTaxLiability() {
        List<TaxLiability> taxLiability = taxLiabilityManagerPort.getAllTaxLiability();
        return ResponseEntity.ok(taxLiabilityRestMapper.toDomain(taxLiability));
    }

    /**
     * Obtiene todos los tipos de contribuyentes.
     *
     * @return lista de tipos de contribuyentes
     */
    @GetMapping("/taxpayertype")
    public ResponseEntity<List<TaxPayerTypeResponse>> getTaxPayer() {
        List<TaxPayerType> taxPayerType = taxPayerTypeManagerPort.getAllTaxPayerTypes();
        return ResponseEntity.ok(taxPayerTypeRestMapper.toDomain(taxPayerType));
    }

    /**
     * Obtiene todas las empresas activas.
     *
     * @return lista de empresas activas
     */
    @Operation(summary = "Obtener todas las empresas activas", description = "Recupera una lista de todas las empresas activas registradas en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresas activas recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnterpriseInfoDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las empresas", content = @Content)
    })
    @GetMapping("/")
    public ResponseEntity<List<EnterpriseInfoDto>> getAllEnterprises() {
        List<EnterpriseInfoDto> enterprises = enterpriseSearchManagerPort.getAllEnterprises();
        return ResponseEntity.ok(enterprises);
    }

    /**
     * Obtiene todas las empresas inactivas.
     *
     * @return lista de empresas inactivas
     */
    @Operation(summary = "Obtener todas las empresas inactivas", description = "Recupera una lista de todas las empresas inactivas registradas en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresas inactivas recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnterpriseInfoDto.class))),
            @ApiResponse(responseCode = "204", description = "No se encontraron empresas inactivas", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las empresas inactivas", content = @Content)
    })
    @GetMapping("/inactive")
    public ResponseEntity<List<EnterpriseInfoDto>> getAllEnterprisesInactive() {
        List<EnterpriseInfoDto> enterprises = enterpriseSearchManagerPort.getAllEnterprisesInactive();
        return ResponseEntity.ok(enterprises);
    }

    /**
     * Crea una nueva empresa
     *
     * @param enterpriseCreateRequest la solicitud de creación de empresa
     * @return la respuesta de creación de empresa
     */
    @Operation(summary = "Crear una empresa", description = "Crea una nueva empresa con la información proporcionada en la solicitud.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnterpriseCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos incompletos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al crear la empresa", content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<EnterpriseCreateResponse> createEnterprise(
            @Valid @RequestBody EnterpriseCreateRequest enterpriseCreateRequest) {
        Enterprise enterprise = enterpriseCreateMapper.toDomain(enterpriseCreateRequest);

        enterprise.setLocation(locationMangerPort.createLocation(enterprise.getLocation()));
        enterprise.setPersonType(personTypeManagerPort.createPersonType(enterprise.getPersonType()));
        enterprise.setIdUser(jwtUtils.getId());

        enterprise = enterpriseCreateMannegerPort.createEnterprise(enterprise);
        return ResponseEntity.ok(enterpriseCreateMapper.toCreateResponse(enterprise));
    }

    /**
     * Actualiza una empresa existente por su ID.
     *
     * @param id el identificador de la empresa a actualizar
     * @param enterpriseCreateRequest la solicitud de actualización de empresa
     * @return la respuesta de la operación
     */
    @Operation(summary = "Actualizar una empresa por id", description = "Actualiza la información de una empresa existente basada en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o empresa no encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al actualizar la empresa", content = @Content)
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEnterprise(@PathVariable("id") UUID id,
            @Valid @RequestBody EnterpriseCreateRequest enterpriseCreateRequest) {

        Enterprise enterpriseExist = enterpriseSearchManagerPort.getEnterpriseById(id);
        if (enterpriseExist == null) {
            return ResponseEntity.badRequest().build();
        }

        Enterprise enterprise = enterpriseCreateMapper.toDomain(enterpriseCreateRequest);
        enterprise.setLocation(locationMangerPort.createLocation(enterprise.getLocation()));
        enterprise.setPersonType(personTypeManagerPort.createPersonType(enterprise.getPersonType()));

        enterpriseUpdateManagerPort.updateEnterprise(id, enterprise);
        locationMangerPort.deleteLocation(enterpriseExist.getLocation().getId());

        return ResponseEntity.ok().build();
    }

    /**
     * Actualiza el estado de una empresa por su ID.
     *
     * @param id el identificador de la empresa
     * @param state el nuevo estado de la empresa
     * @return la respuesta de la operación
     */
    @Operation(summary = "Actualizar el estado de una empresa por id", description = "Permite actualizar el estado de una empresa a ACTIVE, INACTIVE o SUSPENDED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de la empresa actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (estado no válido o error en los datos)"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno al actualizar el estado")
    })
    @PutMapping("/update/status/{id}/{state}")
    public ResponseEntity<?> updateEnterpriseStatus(@PathVariable("id") UUID id, @PathVariable("state") String state) {
        try {
            StateEnum stateEnum;

            switch (state) {
                case "ACTIVE":
                    stateEnum = StateEnum.ACTIVE;
                    break;
                case "INACTIVE":
                    stateEnum = StateEnum.INACTIVE;
                    break;
                case "SUSPENDED":
                    stateEnum = StateEnum.SUSPENDED;
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            enterpriseUpdateManagerPort.updateEnterpriseStatus(id, stateEnum);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene los detalles de una empresa específica por su ID.
     *
     * @param id el identificador de la empresa
     * @return los detalles de la empresa
     */
    @Operation(summary = "Obtener una empresa por ID", description = "Permite obtener los detalles de una empresa específica utilizando su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa encontrada exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")
    })
    @GetMapping("/enterprise/{id}")
    public ResponseEntity<EnterpriseByIdResponse> getEnterpriseById(@PathVariable("id") UUID id) {
        Enterprise enterprise = enterpriseSearchManagerPort.getEnterpriseById(id);
        return ResponseEntity.ok(enterpriseSearchMapper.toEnterpriseByIdResponse(enterprise));
    }

    /**
     * Crea un tercero a partir del contenido de un PDF del RUT.
     *
     * @param file el archivo PDF del RUT
     * @return el contenido extraído del PDF
     */
    @Autowired
    private PdfRUTService pdfRUTService;

    @PostMapping("/content-PDF-RUT")
    public ResponseEntity<PdfRUTContentOutput> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            PdfRUTContent request = new PdfRUTContent(file);
            PdfRUTContentOutput response = pdfRUTService.extractContent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
