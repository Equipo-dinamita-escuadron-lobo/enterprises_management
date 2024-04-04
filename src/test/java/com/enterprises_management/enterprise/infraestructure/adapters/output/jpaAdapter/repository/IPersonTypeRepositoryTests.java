package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IPersonTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
public class IPersonTypeRepositoryTests {
   @Autowired
   private IPersonTypeRepository iPersonTypeRepository;

   private PersonTypeEntity person1;

   @BeforeEach
   void setup(){
      person1=PersonTypeEntity.builder()
              .id(1L)
              .type("persona natural")
              .name("brayan")
              .surname("majin")
              .bussinessName("comercio")
              .build();
   }
   @DisplayName("Test to save a type of person")
   @Test
   void testSavePersonType(){
      //when
      PersonTypeEntity savePersonType=iPersonTypeRepository.save(person1);
      //then
      assertThat(savePersonType).isNotNull();
      assertThat(savePersonType.getId()).isGreaterThan(0);
   }
   @DisplayName("Test to list person type")
   @Test
   void testListPersonType(){
      //given
      PersonTypeEntity person2=PersonTypeEntity.builder()
              .id(2L)
              .type("persona natural")
              .name("Julian")
              .surname("majin")
              .bussinessName("S.A")
              .build();
         iPersonTypeRepository.save(person1);
         iPersonTypeRepository.save(person2);
         //when
      List<PersonTypeEntity> listPersonType=iPersonTypeRepository.findAll();

      //then
      assertThat(listPersonType).isNotNull();
      assertThat(listPersonType.size()).isEqualTo(2);
   }
   @DisplayName("Test list person type to id")
   @Test
   void testGetPersonTypeId(){
      PersonTypeEntity  person2=PersonTypeEntity.builder()
              .id(2L)
              .type("persona natural")
              .name("Freider")
              .surname("Escobar")
              .bussinessName("comercio")
              .build();

      iPersonTypeRepository.save(person2);

      //when
      PersonTypeEntity personBD=iPersonTypeRepository.findById(person2.getId()).get();

      //then
      assertThat(personBD).isNotNull();
   }

   @DisplayName("Test to update type of person")
   @Test
   void testUpdatePersonType(){
      iPersonTypeRepository.save(person1);
      //when
      PersonTypeEntity personTypeSave=iPersonTypeRepository.findById(person1.getId()).get();
      personTypeSave.setName("Andres");
      personTypeSave.setSurname("Hoyos");
      PersonTypeEntity personTypeUpdate=iPersonTypeRepository.save(personTypeSave);
      //then
      assertThat(personTypeUpdate.getName()).isEqualTo("Andres");
      assertThat(personTypeUpdate.getSurname()).isEqualTo("Hoyos");

   }
}
