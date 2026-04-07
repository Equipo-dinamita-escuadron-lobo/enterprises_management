package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ICitiesbyDepartmentRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ICityRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IDepartmentRestMapper;

import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.models.City;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.input.IAddressSearchManagerPort;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;

import lombok.AllArgsConstructor;

/**
 * Controlador REST para gestionar las operaciones relacionadas con direcciones.
 * Proporciona endpoints para obtener información de departamentos y ciudades.
 */
@RequestMapping("/api/enterprises/address")
@RestController
@AllArgsConstructor
public class AddressController {

    private final IAddressSearchManagerPort addressSearchManagerPort;
    private final ICitiesbyDepartmentRestMapper citiesbyDepartmentRestMapper;
    private final IDepartmentRestMapper departmentRestMapper;
    private final ICityRestMapper cityRestMapper;

    /**
     * Obtiene todos los países disponibles en el sistema.
     *
     * @return ResponseEntity con la lista de países
     */
    @GetMapping("/countries")
    @Operation(summary = "Obtener todos los países", description = "Obtener una lista de todos los países disponibles en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Países obtenidos exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno al obtener los países")
    })
    public ResponseEntity<List<Country>> getAllCountries() {
        List<Country> countries = addressSearchManagerPort.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    /**
     * Obtiene todos los departamentos disponibles en el sistema por país.
     *
     * @param idCountry el identificador del país
     * @return ResponseEntity con la lista de departamentos
     */
    @GetMapping("/countries/{idCountry}/departments")
    public ResponseEntity<?> getDepartmentsByCountry(
            @PathVariable("idCountry") String idCountry) {

        if (idCountry == null || idCountry.equals("undefined")) {
            return ResponseEntity.badRequest().body("idCountry inválido");
        }

        Long id;
        try {
            id = Long.parseLong(idCountry);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("idCountry debe ser numérico");
        }

        List<Department> departments = addressSearchManagerPort.getAllDepartments(id);
        return ResponseEntity.ok(departmentRestMapper.toDepartmentResponseList(departments));
    }

    /**
     * Obtiene todas las ciudades asociadas a un departamento específico.
     *
     * @param idDepartment el identificador del departamento
     * @return ResponseEntity con la lista de ciudades del departamento
     */
    @GetMapping("/departments/{idDepartment}/cities")
    @Operation(summary = "Obtener ciudades por departamento", description = "Recupera todas las ciudades asociadas a un departamento específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ciudades recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CityResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Departamento no encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las ciudades", content = @Content)
    })
    public ResponseEntity<List<CityResponseDto>> getAllCities(
            @PathVariable("idDepartment") Long idDepartment) {
        List<City> cities = addressSearchManagerPort.getAllCities(idDepartment);
        List<CityResponseDto> cityResponseDtos = cityRestMapper.toResponseList(cities);
        return ResponseEntity.ok(cityResponseDtos);
    }
}
