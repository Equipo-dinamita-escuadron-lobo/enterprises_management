package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.EnterprisesManagementApplication;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.*;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
public class IEnterpriseRepositoryTests {
    @Autowired
    private IEnterpriseRepository iEnterpriseRepository;

    @Autowired
    private TestEntityManager entityManager;


    private EnterpriseEntity enterpriseEntity;
    @BeforeEach
    void setup(){
        TaxLiabilityEntity taxLiability=TaxLiabilityEntity.builder()
                .name("facturador electronico")
                .build();
        List<TaxLiabilityEntity> listTaxLiability=new ArrayList<>();
        listTaxLiability.add(taxLiability);

        TaxPayerTypeEntity taxPayerType=TaxPayerTypeEntity.builder()
                .name("resonsable de iva")
                .build();
        taxPayerType=entityManager.persistAndFlush(taxPayerType);

        EnterpriseTypeEntity enterpriseType= EnterpriseTypeEntity.builder()
                .name("privada")
                .build();
        enterpriseType=entityManager.persistAndFlush( enterpriseType);

        PersonTypeEntity person= PersonTypeEntity.builder()
                .type("persona natural")
                .name("brayan")
                .surname("majin")
                .bussinessName("comercio")
                .build();
        person= entityManager.persistAndFlush( person);

        CountryEntity country = CountryEntity.builder()
                .name("TestCountry")
                .build();
        country = entityManager.persistAndFlush(country);

       DepartmentEntity department = DepartmentEntity.builder()
                .country(country)
                .name("TestDepartment")
                .build();
        department = entityManager.persistAndFlush(department);

        CityEntity   city = CityEntity.builder()
                .department(department)
                .name("TestCity")
                .build();
        city = entityManager.persistAndFlush(city);

        LocationEntity location=LocationEntity.builder()
                .address("vereda Pomona")
                .country(country)
                .department(department)
                .city(city)
                .build();
        location= entityManager.persistAndFlush(location);

        enterpriseEntity=EnterpriseEntity.builder()
                .name("Exito")
                .nit("4441555")
                .phone("3136453524")
                .branch("si")
                .email("exito@gmail.com")
                .logo("www.exito.com")
                .taxLiabilities(listTaxLiability)
                .taxPayerType(taxPayerType)
                .enterpriseType(enterpriseType)
                .personType(person)
                .location (location)
                .build();

    }
    @DisplayName("Test to save enterprise")
    @Test
    void testSaveEnterprise(){

        //when
        EnterpriseEntity enterpriseEntitySave=iEnterpriseRepository.save(enterpriseEntity);
        //then
        assertThat(enterpriseEntitySave).isNotNull();
        assertThat(enterpriseEntitySave.getId()).isGreaterThan(0);
    }
    @DisplayName("Test to get enterprise by Id")
    @Test
    void testGetEnterpriseById(){
        iEnterpriseRepository.save(enterpriseEntity);
        //when
        EnterpriseEntity enterpriseEntityBD=iEnterpriseRepository.findById(enterpriseEntity.getId()).get();
        //then
        assertThat(enterpriseEntityBD).isNotNull();
    }
    @DisplayName("Test to get list enterprise")
    @Test
    void testGetListEnterprise(){
        TaxLiabilityEntity taxLiability2=TaxLiabilityEntity.builder()
                .name("informante de beneficiarios finales")
                .build();
        List<TaxLiabilityEntity> listTaxLiability2=new ArrayList<>();
        listTaxLiability2.add(taxLiability2);

        TaxPayerTypeEntity taxPayerType2=TaxPayerTypeEntity.builder()
                .name("regimen simple de tributación")
                .build();
        taxPayerType2=entityManager.persistAndFlush(taxPayerType2);

        EnterpriseTypeEntity enterpriseType2= EnterpriseTypeEntity.builder()
                .name("mixta")
                .build();
        enterpriseType2=entityManager.persistAndFlush( enterpriseType2);

        PersonTypeEntity person2= PersonTypeEntity.builder()
                .type("persona natural")
                .name("julian")
                .surname("Ruano")
                .bussinessName("comercio")
                .build();
        person2= entityManager.persistAndFlush( person2);

        CountryEntity country2 = CountryEntity.builder()
                .name("Colombia")
                .build();
        country2 = entityManager.persistAndFlush(country2);

        DepartmentEntity department2 = DepartmentEntity.builder()
                .country(country2)
                .name("Antioquia")
                .build();
        department2 = entityManager.persistAndFlush(department2);

        CityEntity   city2 = CityEntity.builder()
                .department(department2)
                .name("Medellin")
                .build();
        city2 = entityManager.persistAndFlush(city2);

        LocationEntity location2=LocationEntity.builder()
                .address("santa Elena")
                .country(country2)
                .department(department2)
                .city(city2)
                .build();
        location2= entityManager.persistAndFlush(location2);

        EnterpriseEntity enterpriseEntity2=EnterpriseEntity.builder()
                .name("Maxipan")
                .nit("5445877789")
                .phone("3136453524")
                .branch("si")
                .email("maxipan@gmail.com")
                .logo("www.maxipan.com")
                .taxLiabilities(listTaxLiability2)
                .taxPayerType(taxPayerType2)
                .enterpriseType(enterpriseType2)
                .personType(person2)
                .location (location2)
                .build();
        iEnterpriseRepository.save(enterpriseEntity);
        iEnterpriseRepository.save(enterpriseEntity2);

        //when
        List<EnterpriseEntity> listEnterprise=iEnterpriseRepository.findAll();
        //then
        assertThat(listEnterprise).isNotNull();
        assertThat(listEnterprise.size()).isEqualTo(2);
    }
    @DisplayName("Test to update enterprise")
    @Test
    void testUpdateInterprise(){
        iEnterpriseRepository.save(enterpriseEntity);
        //when
            EnterpriseEntity enterpriseSave=iEnterpriseRepository.findById(enterpriseEntity.getId()).get();
             enterpriseSave.setName("Carreful");
             enterpriseSave.setNit("12345678");
             enterpriseSave.setEmail("carreful@gmail.com");

             EnterpriseEntity enterpriseUpdate=iEnterpriseRepository.save(enterpriseSave);
             //then
        assertThat(enterpriseUpdate.getName()).isEqualTo("Carreful");
        assertThat(enterpriseUpdate.getNit()).isEqualTo("12345678");
        assertThat(enterpriseUpdate.getEmail()).isEqualTo("carreful@gmail.com");


    }
    @DisplayName("Test to delete enterprise")
    @Test
    void testDeleteInterprise(){
        iEnterpriseRepository.save(enterpriseEntity);
        //when
        iEnterpriseRepository.deleteById(enterpriseEntity.getId());
        Optional<EnterpriseEntity> enterpriseOptional=iEnterpriseRepository.findById(enterpriseEntity.getId());

        //than
        assertThat(enterpriseOptional).isEmpty();
    }

}
