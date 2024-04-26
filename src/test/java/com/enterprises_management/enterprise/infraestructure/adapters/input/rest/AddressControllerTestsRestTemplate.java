/* 
package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CityDTO;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.CitiesbyDepartmentResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig
public class AddressControllerTestsRestTemplate {
     @Autowired
     private TestRestTemplate testRestTemplate;
     private CityDTO cityDTO;
    @Test
    @Order(1)

    void testGetAllDepartment(){

        ResponseEntity<DepartmentAddressResponse[]> response=testRestTemplate.getForEntity("http://localhost:8080/api/address/departments", DepartmentAddressResponse[].class);
       List<DepartmentAddressResponse> departments=Arrays.asList(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON,response.getHeaders().getContentType());
        assertEquals(32,departments.size());
    }
    @Test
    @Order(2)
    @SqlGroup({
            @Sql("/data.sql"),

    })
    void testGetAllCities(){


        Map<Long, List<String>> departmentCitiesMap = new HashMap<>();
       // departmentCitiesMap.put(2L, Arrays.asList("Medellin", "Bello", "Itagui","Envigado","Rionegro"));
        departmentCitiesMap.put(2L, Arrays.asList("Tunja", "Sogamoso", "Duitama"));

        ResponseEntity<DepartmentAddressResponse[]> departmentResponse = testRestTemplate.getForEntity("/api/address/departments", DepartmentAddressResponse[].class);
        Long departmentId = departmentResponse.getBody()[5].getId();

        ResponseEntity<CitiesbyDepartmentResponse> citiesResponse = testRestTemplate.getForEntity("/api/address/cities/" + departmentId, CitiesbyDepartmentResponse.class);


        assertEquals(HttpStatus.OK, citiesResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, citiesResponse.getHeaders().getContentType());

        CitiesbyDepartmentResponse citiesData = citiesResponse.getBody();
        assertNotNull(citiesData);
        assertNotNull(citiesData.getCities());


        List<String> expectedCities = departmentCitiesMap.get(2L);

        // Verificar que las ciudades obtenidas coinciden con las esperadas
        List<String> actualCities = citiesData.getCities().stream()
                .map(CityDTO::getName)
                .collect(Collectors.toList());
        assertEquals(expectedCities, actualCities);
        assertTrue(actualCities.size() > 0);
        assertEquals(3,actualCities.size());
    }

}
*/