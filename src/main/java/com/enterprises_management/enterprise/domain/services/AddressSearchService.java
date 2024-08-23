package com.enterprises_management.enterprise.domain.services;

import com.enterprises_management.enterprise.application.ports.input.IAddressSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IAddressSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Department;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;


@AllArgsConstructor
@Service
public class AddressSearchService implements IAddressSearchManagerPort {

    private final IAddressSearchOutputPort addressSearchOutputPort;


    @Override
    public List<Department> getAllDepartment(){

        return addressSearchOutputPort.getAllDepartment();
    }

    @Override
    public Department getAllCities(Long idDepartment) {
        return addressSearchOutputPort.getAllCities( idDepartment);

    }



}
