package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import com.enterprises_management.enterprise.application.ports.output.IAddressSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IAddressMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IAddressRepository;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IDepartmentAddressRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AddressJpaAdapter implements IAddressSearchOutputPort {
 private final IAddressRepository addressRepository;
 private final IDepartmentAddressRepository departmentAddressRepository;
 private final IAddressMapper addressMapper;

    @Override
    public Department getDepartment(Long id){

        DepartmentEntity departmentEntities =departmentAddressRepository.findAllById(id);
        Department deparment=addressMapper.tolistModelDeparment(departmentEntities);

        return deparment;
    }

    @Override
    public City getCity(Long id) {
       CityEntity cityEntities= addressRepository.findAllById(id);
        City city=addressMapper.tolistModelCity(cityEntities);

        return city;

    }
}
