package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxLiabilityRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnterpriseControllerTestRestTemplate {
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ITaxLiabilityRepository iTaxLiabilityRepository;
    @Test
    @Order(1)
    void testgetAllTaxLiability(){
        
        ResponseEntity<TaxLiabilityResponse[]> response=testRestTemplate.getForEntity("http://localhost:8080/enterprises/taxliabilities",  TaxLiabilityResponse[].class);
        List<TaxLiabilityResponse> enterprise= Arrays.asList(response.getBody());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON,response.getHeaders().getContentType());

        assertEquals(4,enterprise.size());
    }
}
