package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@ActiveProfiles("test")
public class IDepartmentAddressRepositoryTests {
    @Autowired
    private IDepartmentAddressRepository iDepartmentAddressRepository;

    @DisplayName("Test to save Department")
    @Test
    void testSaveDepartment(){
        CountryEntity country=CountryEntity.builder()

                .name("Colombia")
                .build();

        DepartmentEntity department=DepartmentEntity.builder()
                .name("Ñariño")
                .country(country)
                .build();
        DepartmentEntity departmentSave=iDepartmentAddressRepository.save(department);

        assertThat(departmentSave).isNotNull();
        assertThat(departmentSave.getId()).isGreaterThan(0);
    }
    @DisplayName("Test to get Department by Id")
    @Test
    void testGetDepartmentById(){
        CountryEntity country=CountryEntity.builder()
                .name("Colombia")
                .build();

        DepartmentEntity department=DepartmentEntity.builder()
                .name("Antioquia")
                .country(country)
                .build();
        iDepartmentAddressRepository.save(department);
        //when
        DepartmentEntity departmentBd=iDepartmentAddressRepository.findById(department.getId()).get();

        //then
        assertThat(departmentBd).isNotNull();
    }

    @DisplayName("Test to get list of departments")
    @Test
    void testGetListDepartments(){
        CountryEntity country=CountryEntity.builder()
                .name("Colombia")
                .build();

        DepartmentEntity department=DepartmentEntity.builder()
                .name("Antioquia")
                .build();
        DepartmentEntity department2=DepartmentEntity.builder()
                .name("Cesar")
                .build();
        iDepartmentAddressRepository.save(department);
        iDepartmentAddressRepository.save(department2);

        //when
        List<DepartmentEntity> listDepartments=iDepartmentAddressRepository.findAll();

        //then
        assertThat(listDepartments).isNotNull();
        assertThat(listDepartments.size()).isEqualTo(2);
    }
}
