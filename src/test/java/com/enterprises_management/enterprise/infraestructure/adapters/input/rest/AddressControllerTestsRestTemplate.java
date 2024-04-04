package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AddressControllerTestsRestTemplate {
    // @Autowired
    // private TestRestTemplate testRestTemplate;
/*
    @Test
    @Order(1)
    void testGetAllDepartment(){

        List<DepartmentAddressResponse> expectedDepartments = Arrays.asList(
                new DepartmentAddressResponse(1L, "Department 1"),
                new DepartmentAddressResponse(2L, "Department 2")
        );

        ResponseEntity<List<DepartmentAddressResponse>> respuesta=testRestTemplate.getForEntity("http://localhost:8080/address/departments", expectedDepartments.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDepartments, response.getBody());
    }*/

}
