package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.CitiesbyDepartmentResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.ICitiesbyDepartmentRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.IDepartmentRestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.input.IAddressSearchManagerPort;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;

import lombok.AllArgsConstructor;

@RequestMapping("/address")
@RestController
@AllArgsConstructor
public class AddressController {


    private final IAddressSearchManagerPort addressSearchManagerPort;
    private final ICitiesbyDepartmentRestMapper citiesbyDepartmentRestMapper;
    private final IDepartmentRestMapper departmentRestMapper;


    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentAddressResponse>> getAllDepartment(){
        List<Department> departments =  addressSearchManagerPort.getAllDepartment();
        return ResponseEntity.ok(departmentRestMapper.toDepartmentResponseList(departments));
    }
    @GetMapping("/cities/{idDepartment}")
    public ResponseEntity<CitiesbyDepartmentResponse> getAllCities(@PathVariable("idDepartment") Long idDepartment){
        Department department =addressSearchManagerPort.getAllCities( idDepartment);
        CitiesbyDepartmentResponse citiesbyDepartmentResponse=citiesbyDepartmentRestMapper.toResponse(department);
        return ResponseEntity.ok(citiesbyDepartmentResponse);

    }
}
