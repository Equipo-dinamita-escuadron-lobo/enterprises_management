package com.enterprises_management.enterprise.domain.service;

import com.enterprises_management.enterprise.application.ports.output.IAddressSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.services.AddressSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.assertThat;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AddressSearchServiceTest {

    @Mock
    private IAddressSearchOutputPort addressSearchOutputPort;
    @InjectMocks
    private AddressSearchService addressSearchService;

    private Department department;
    private Department department2;
    @BeforeEach
    void setup(){

        Long idDepartment = 1L;
        Country country = Country.builder()
                .id(1L)
                .name("Colombia")
                .build();

         department = Department.builder()
                .id(idDepartment)
                .name("Department1")
                .country(country)
                .cities(new ArrayList<>())
                .build();

        City city1 = City.builder()
                .id(1L)
                .name("City1")
                .department(department)
                .build();
        City city2 = City.builder()
                .id(2L)
                .name("City2")
                .department(department)
                .build();

        department.getCities().add(city1);
        department.getCities().add(city2);

        //configuracion del departamento 2

        Country country2 = Country.builder()
                .id(1L)
                .name("Colombia")
                .build();

        department2 = Department.builder()
                .id(1L)
                .name("Department2")
                .country(country2)
                .cities(new ArrayList<>())
                .build();

        City city3 = City.builder()
                .id(1L)
                .name("City3")
                .department(department)
                .build();
        City city4 = City.builder()
                .id(2L)
                .name("City4")
                .department(department)
                .build();

        department2.getCities().add(city3);
        department2.getCities().add(city4);
    }

    @DisplayName("Test para listar las ciudades de un departamento")
    @Test
    void testGetAllCities() {



        given(addressSearchOutputPort.getAllCities(1L)).willReturn(department);

        Department result = addressSearchService.getAllCities(1L);





        assertNotNull(result);
        assertEquals(department.getId(), result.getId());
        assertEquals(department.getName(), result.getName());
        assertEquals(department.getCities().size(), result.getCities().size());
        assertEquals(department.getCities().get(0).getName(), result.getCities().get(0).getName());
        assertEquals(department.getCities().get(0).getDepartment().getId(), result.getCities().get(0).getDepartment().getId());


        verify(addressSearchOutputPort, times(1)).getAllCities(1L);
    }
    @DisplayName("Test para listar todos los departamentos")
    @Test
    void testGetAllDepartment(){
        //given
        given(addressSearchOutputPort.getAllDepartment()).willReturn(List.of(department,department2));

        //when
        List<Department> departaments=addressSearchService.getAllDepartment();

        //then
        assertThat(departaments).isNotNull();
        assertThat(departaments.size()).isEqualTo(2);


    }
}
