package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import com.enterprises_management.enterprise.application.ports.output.IAddressSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ICitiesbyDepartmentMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IDepartmentsMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IAddressRepository;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IDepartmentAddressRepository;
import lombok.Data;

import java.util.List;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class AddressJpaAdapter implements IAddressSearchOutputPort {
 private final IAddressRepository addressRepository;
 private final IDepartmentAddressRepository departmentAddressRepository;
 private final ICitiesbyDepartmentMapper citiesbyDepartmentMapper;
 private final IDepartmentsMapper departmentsMapper;




    @Override
    public List<Department> getAllDepartment(){
        return departmentsMapper.toModelList(departmentAddressRepository.findAll());
    }

    @Override
    public Department getAllCities(Long idDepartment) {
       return citiesbyDepartmentMapper.toDomain(departmentAddressRepository.findById(idDepartment).get());
    }


}
