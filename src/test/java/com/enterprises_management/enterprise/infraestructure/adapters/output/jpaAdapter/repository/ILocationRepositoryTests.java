package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;



import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@ActiveProfiles("test")
public class ILocationRepositoryTests {
    @Autowired
    private ILocationRepository iLocationRepository;
    @Autowired
    private TestEntityManager entityManager;

    private  CountryEntity country;

    private DepartmentEntity department;

    private CityEntity city;

    @BeforeEach
    void setup(){
         country = CountryEntity.builder()
                .name("TestCountry")
                .build();
        country = entityManager.persistAndFlush(country);

        department = DepartmentEntity.builder()
                .country(country)
                .name("TestDepartment")
                .build();
        department = entityManager.persistAndFlush(department);

        city = CityEntity.builder()
                .department(department)
                .name("TestCity")
                .build();
        city = entityManager.persistAndFlush(city);

    }
    @DisplayName("Test to save location")
    @Test
    void testSaveLocation(){



        LocationEntity location=LocationEntity.builder()
            .address("vereda Pomona")
                .country(country)
                .department(department)
                .city(city)
                .build();
        //when
        LocationEntity locationSave=iLocationRepository.save(location);
        //then
        assertThat(locationSave).isNotNull();
        assertThat(locationSave.getId()).isGreaterThan(0);
    }

    @DisplayName("Test to get location by Id")
    @Test
    void testGetLocationById(){
        LocationEntity location=LocationEntity.builder()
                .address("santaClara")
                .country( country)
                .department(department)
                .city(city)
                .build();

        iLocationRepository.save(location);

        //when
        LocationEntity locationSave=iLocationRepository.findById(location.getId()).get();
        //then
        assertThat(locationSave).isNotNull();

    }
    @DisplayName("Test to get list location")
    @Test
    void testGetListLocations(){
        LocationEntity location=LocationEntity.builder()
                .address("santaClara")
                .country( country)
                .department(department)
                .city(city)
                .build();
        LocationEntity location2=LocationEntity.builder()
                .address("santaClara")
                .country( country)
                .department(department)
                .city(city)
                .build();

        iLocationRepository.save(location);
        iLocationRepository.save(location2);

        //when
        List<LocationEntity> listLocations=iLocationRepository.findAll();

        //then
        assertThat(listLocations).isNotNull();
        assertThat(listLocations.size()).isEqualTo(2);
    }
    @DisplayName("Test to Update location")
    @Test
    void testUpdateLocation(){
        LocationEntity location=LocationEntity.builder()
                .address("vereda Pomona")
                .country(country)
                .department(department)
                .city(city)
                .build();
        iLocationRepository.save(location);

        //when
        LocationEntity locationSave=iLocationRepository.findById(location.getId()).get();
        locationSave.setAddress("Bello Horizonte");
        LocationEntity locationUpdate=iLocationRepository.save(locationSave);

        //then
          assertThat(locationUpdate.getAddress()).isEqualTo("Bello Horizonte");
    }
    @DisplayName("Test to delete location")
    @Test
    void testDeleteLocation(){
        LocationEntity location=LocationEntity.builder()
                .address("santaClara")
                .country( country)
                .department(department)
                .city(city)
                .build();

        iLocationRepository.save(location);

        //when
        iLocationRepository.deleteById(location.getId());
        Optional<LocationEntity> locationOptional=iLocationRepository.findById(location.getId());

        //then
        assertThat(locationOptional).isEmpty();
    }

}
