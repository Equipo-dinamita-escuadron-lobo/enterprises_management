package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxLiabilityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
public class ITaxLiabilityRepositoryTests {
 @Autowired
    private ITaxLiabilityRepository iTaxLiabilityRepository;

  @DisplayName("Test to save taxLiability")
 @Test
  void testSaveTaxLiability(){
      TaxLiabilityEntity taxLiability=TaxLiabilityEntity.builder()
              .name("facturador electronico")
              .build();
      //when
      TaxLiabilityEntity taxLiabilitySave=iTaxLiabilityRepository.save(taxLiability);
      //then
      assertThat(taxLiabilitySave).isNotNull();
      assertThat(taxLiabilitySave.getId()).isGreaterThan(0);
  }
  @DisplayName("Test to get taz liability by Id")
  @Test
  void testGetTaxLiabilityById(){
      TaxLiabilityEntity taxLiability=TaxLiabilityEntity.builder()
              .name("facturador electronico")
              .build();
      iTaxLiabilityRepository.save(taxLiability);
      //when
      TaxLiabilityEntity taxLiabilityBD=iTaxLiabilityRepository.findById(taxLiability.getId()).get();

      //then
      assertThat(taxLiabilityBD).isNotNull();
  }
    @DisplayName("Test to get  list tax liability")
    @Test
    void testGetListTaxLiabilities(){
        TaxLiabilityEntity taxLiability=TaxLiabilityEntity.builder()
                .name("facturador electronico")
                .build();
        TaxLiabilityEntity taxLiability2=TaxLiabilityEntity.builder()
                .name("autorretenedor")
                .build();

        iTaxLiabilityRepository.save(taxLiability);
        iTaxLiabilityRepository.save(taxLiability2);

        //when
        List<TaxLiabilityEntity> listTaxLiabilityEntities=iTaxLiabilityRepository.findAll();

        //then
        assertThat(listTaxLiabilityEntities).isNotNull();
        assertThat(listTaxLiabilityEntities.size()).isEqualTo(2);
    }

}
