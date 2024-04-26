/* 
package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
@DataJpaTest
@ActiveProfiles("test")
public class IAddressRepositoryTests {
    @Autowired
    private IAddressRepository iAddressRepository;



    @DisplayName("Test to Save Cities")
    @Test
    void testSaveCity(){

         CountryEntity country=CountryEntity.builder()
                .id(1L)
                .name("Colombia")
                .build();

         DepartmentEntity    department1=DepartmentEntity.builder()
                .name("Cauca")
                 .country(country)
                .build();

        CityEntity city=CityEntity.builder()
                .name("Popayan")
                .department(department1)
                .build();

        CityEntity saveCity=iAddressRepository.save(city);

        assertThat(saveCity).isNotNull();
        assertThat(saveCity.getId()).isNotNull();
        assertThat(saveCity.getId()).isGreaterThan(0);
    }
    @DisplayName("Test to get city by id")
    @Test
    void testGetCityById(){

        CountryEntity country=CountryEntity.builder()
                .id(1L)
                .name("Colombia")
                .build();

        DepartmentEntity    department1=DepartmentEntity.builder()
                .name("Valle del Cauca")
                .country(country)
                .build();

        CityEntity city=CityEntity.builder()
                .name("Cali")
                .department(department1)
                .build();

        iAddressRepository.save(city);
        //when
        CityEntity cityBD=iAddressRepository.findById(city.getId()).get();
        //then
        assertThat(cityBD).isNotNull();

    }
    @DisplayName("test to obtain a list of cities ")
    @Test
    void testGetListCities(){
        CountryEntity country=CountryEntity.builder()
                .id(1L)
                .name("Colombia")
                .build();



        DepartmentEntity    department1=DepartmentEntity.builder()
                .name("Valle del Cauca")
                .country(country)
                .build();

        CityEntity city=CityEntity.builder()
                .name("Cali")
                .build();
        CityEntity city2=CityEntity.builder()
                .name("palmira")
                .build();
        iAddressRepository.save(city);
        iAddressRepository.save(city2);
        //when
        List<CityEntity> listCities=iAddressRepository.findAll();

        assertThat(listCities).isNotNull();
        assertThat(listCities.size()).isEqualTo(2);
    }
}
*/
