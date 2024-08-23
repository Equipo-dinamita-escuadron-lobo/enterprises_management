package com.enterprises_management.enterprise.domain.service;
import com.enterprises_management.enterprise.application.ports.output.ITaxLiabilityOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.services.TaxLiabilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class TaxLiabilityServiceTest {
        @Mock
        private ITaxLiabilityOutputPort taxLiabilityOutputPort;

        @InjectMocks
        private TaxLiabilityService taxLiabilityService;

        private TaxLiability  taxLiability1;
        private TaxLiability  taxLiability2;
        @BeforeEach
        void setup(){
             taxLiability1=TaxLiability.builder()
                    .id(1L)
                    .name("taxLiability1")
                    .build();
           taxLiability2=TaxLiability.builder()
                    .id(2L)
                    .name("taxLiability2")
                    .build();
        }


    @DisplayName("Test para listar las responsabilidades tributarias")
    @Test
    void testGetAllTaxLiability() {

        List<TaxLiability> expectedTaxLiabilities = Arrays.asList(taxLiability1, taxLiability2);
        given(taxLiabilityOutputPort.getAll()).willReturn(expectedTaxLiabilities);


        List<TaxLiability> actualTaxLiabilities = taxLiabilityService.getAllTaxLiability();


        assertEquals(expectedTaxLiabilities, actualTaxLiabilities);
        then(taxLiabilityOutputPort).should().getAll();
    }
}
