package com.enterprises_management.enterprise.domain.service;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseCreateOutputPort;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.*;
import com.enterprises_management.enterprise.domain.services.EnterpriseCreateService;
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
import java.util.UUID;


import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class EnterpriseCreateServiceTest {

    @Mock
    private IEnterpriseCreateOutputPort enterpriseCreateOutputPort;

    @InjectMocks
    private EnterpriseCreateService enterpriseCreateService;

    private Enterprise enterprise;
    @BeforeEach
    void setup(){
        TaxLiability taxLiability1=TaxLiability.builder()
                .id(1L)
                .name("taxLiability1")
                .build();
        TaxLiability taxLiability2=TaxLiability.builder()
                .id(2L)
                .name("taxLiability2")
                .build();

        List<TaxLiability> taxLiabilityList=new ArrayList<>();
        taxLiabilityList.add(taxLiability1);
        taxLiabilityList.add(taxLiability2);

        TaxPayerType taxPayerType=TaxPayerType.builder()
                .id(1L)
                .name("taxPayerType1")
                .build();
        EnterpriseType enterpriseType=EnterpriseType.builder()
                .id(1L)
                .name("enterpriseType1")
                .build();
        PersonType personType=PersonType.builder()
                .id(1L)
                .type("type1")
                .name("name1")
                .surname("surname1")
                .bussinessName("bussinessName")
                .build();


        Long idDepartment = 1L;
        Country country = Country.builder()
                .id(1L)
                .name("Colombia")
                .build();

        Department department = Department.builder()
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

        Location location= Location.builder()
                .id(1L)
                .address("address1")
                .city(city1)
                .country(country)
                .department(department)
                .build();


      enterprise=Enterprise.builder()
                .id(UUID.randomUUID())
                .idUser("1")
                .name("enterprise1")
                .nit("1111")
                .DV("DV1")
                .phone("3124525585")
                .branch("comercio")
                .email("enterprise@gmail.com")
                .logo("www.logo.com")
                .state(StateEnum.ACTIVE)
                .taxLiabilities(taxLiabilityList)
                .taxPayerType(taxPayerType)
                .enterpriseType(enterpriseType)
                .personType(personType)
                .location(location)
                .build();

    }
    @DisplayName("")
    @Test
    void testCreateEnterprise(){
    //given
        given(enterpriseCreateOutputPort.create(enterprise)).willReturn(enterprise);

        //when
        Enterprise enterpriseSave=enterpriseCreateService.createEnterprise(enterprise);

        //then
        assertThat(enterpriseSave).isNotNull();
        assertThat(enterpriseSave.getName()).isEqualTo(enterprise.getName());

    }




}
