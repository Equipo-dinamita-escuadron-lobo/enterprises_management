package com.enterprises_management.enterprise.domain.service;
import com.enterprises_management.enterprise.application.ports.output.ILocationOutputPort;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.domain.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {
    @Mock
    private ILocationOutputPort locationOutputPort;

    @InjectMocks
    private LocationService locationService;
    private Location location;
    @BeforeEach
    void setup(){
        Country country = Country.builder()
                .id(1L)
                .name("Colombia")
                .build();

        Department department = Department.builder()
                .id(1L)
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

      location= Location.builder()
                .id(1L)
                .address("address1")
                .city(city1)
                .country(country)
                .department(department)
                .build();
    }
    @DisplayName("Test para crear ubicación")
    @Test
    void testCreateLocation() {
        //given
        given(locationOutputPort.create(location)).willReturn(location);
        //when

        Location createdLocation = locationService.createLocation(location);
        //then
        assertEquals(location, createdLocation);
        then(locationOutputPort).should().create(location);
    }

    @DisplayName("Test para eliminar ubicación")
    @Test
    void testDeleteLocation() {
        // Arrange
        Long locationId = 1L;
        given(locationOutputPort.delete(locationId)).willReturn(true);

        // Act
        boolean isDeleted = locationService.deleteLocation(locationId);

        // Assert
        assertTrue(isDeleted);
        then(locationOutputPort).should().delete(locationId);
    }

}
