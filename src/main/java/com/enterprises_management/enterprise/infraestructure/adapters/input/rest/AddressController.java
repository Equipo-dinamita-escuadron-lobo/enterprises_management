package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.output.IAddressSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.CityAddressResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.IAddressRestMapper;

import lombok.AllArgsConstructor;

@RequestMapping("/address")
@RestController
@AllArgsConstructor
public class AddressController {
    private final IAddressSearchOutputPort  addressSearchOutputPort;
    private final  IAddressRestMapper addressRestMapper;
    @GetMapping("/city/{id}")
    public ResponseEntity<List<CityAddressResponse>> getCity(@PathVariable("id") Long id){
        City city=addressSearchOutputPort.getCity(id);
        return ResponseEntity.ok(Collections.singletonList(addressRestMapper.ToCityResponseList(city)));
    }
    @GetMapping("/department/{id}")
    public ResponseEntity<List<DepartmentAddressResponse>> getDepartment(@PathVariable("id") Long idCountry){
        Department department=addressSearchOutputPort.getDepartment(idCountry);
        return ResponseEntity.ok(Collections.singletonList(addressRestMapper.toDepartmentResponseList(department)));
    }
}
