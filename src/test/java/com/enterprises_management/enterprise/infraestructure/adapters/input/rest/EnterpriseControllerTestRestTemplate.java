
/* 
package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.*;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.LocationDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.PersonTypeDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*; 

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig
//@Sql(scripts = {"/data.sql"})

public class EnterpriseControllerTestRestTemplate {    
    @Autowired
    private TestRestTemplate testRestTemplate;

   private  EnterpriseCreateRequest enterprise;


    @BeforeEach

    void setup(){



        PersonType person= PersonType.builder()
                .type("persona natural")
                .name("brayan")
                .surname("majin")
                .bussinessName("comercio")
                .build();

        LocationDto location= LocationDto.builder()
                .address("La esmeralda")
                .country(1L)
                .department(1L)
                .city(1L)
                .build();

        List<Long> listTaxLiability = List.of(1L,2L);

        PersonTypeDto personTypeDto=PersonTypeDto.builder()
                .type("persona natural")
                .name("brayan")
                .surname("majin")
                .bussinessName("comercio")
                .build();

        enterprise= EnterpriseCreateRequest.builder()
                .id(1L)
                .name("Super Pollos")
                .nit("12345678")
                .DV("si")
                .phone("3136448975")
                .branch("si")
                .email("superpollos@gmail.com")
                .logo("www.superpollos.com")
                .taxLiabilities(listTaxLiability)
                .taxPayerType(1L)
                .enterpriseType(1L)
                .personType(personTypeDto)
                .location(location)
                        .build();




    }
    @Test
    @Order(1)
    void testgetAllTaxLiability(){

        ResponseEntity<TaxLiabilityResponse[]> response=testRestTemplate.getForEntity("http://localhost:8080/api/enterprises/taxliabilities",  TaxLiabilityResponse[].class);
        List<TaxLiabilityResponse> enterprise= Arrays.asList(response.getBody());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON,response.getHeaders().getContentType());

        assertEquals(4,enterprise.size());
    }
    @Test
    @Order(2)
    @SqlGroup({
            @Sql("/data.sql"),

    })
    void  testCreateEnterprise() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Crear HttpEntity
        HttpEntity<EnterpriseCreateRequest> entity = new HttpEntity<>(enterprise, headers);

        ResponseEntity<EnterpriseCreateResponse> response = testRestTemplate.postForEntity("http://localhost:8080/api/enterprises/",entity, EnterpriseCreateResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        EnterpriseCreateResponse enterpriseCreate=response.getBody();
        assertNotNull(enterpriseCreate);
        assertEquals("Super Pollos",enterpriseCreate.getName());
        assertEquals("12345678",enterpriseCreate.getNit());
        assertEquals("3136448975",enterpriseCreate.getPhone());
        assertEquals("superpollos@gmail.com",enterpriseCreate.getEmail());
    }
    @Test
    @Order(3)

    void testGetEnterprise(){
        ResponseEntity<EnterpriseInfoDto[]> response=testRestTemplate.getForEntity("http://localhost:8080/api/enterprises/",EnterpriseInfoDto[].class);
        List<EnterpriseInfoDto> enterprises=Arrays.asList(response.getBody());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON,response.getHeaders().getContentType());
        assertTrue(enterprises.size()>0);
        assertEquals(1L,enterprises.get(0).getId());
    }

} */

