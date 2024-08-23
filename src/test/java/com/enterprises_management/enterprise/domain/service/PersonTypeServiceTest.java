package com.enterprises_management.enterprise.domain.service;
import com.enterprises_management.enterprise.application.ports.output.IPersonTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.services.PersonTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;


import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PersonTypeServiceTest {
    @Mock
    private IPersonTypeOutputPort personTypeOutputPort;

    @InjectMocks
    private PersonTypeService personTypeService;
    private PersonType personType;
    @BeforeEach
    void setup(){

        personType=PersonType.builder()
                .id(1L)
                .type("type1")
                .name("name1")
                .surname("surname1")
                .bussinessName("bussinessName")
                .build();
    }

    @DisplayName("Test para guardar tipo de persona")
    @Test
    void testCreatePersonType(){
        //given
        given(personTypeOutputPort.create(personType)).willReturn(personType);

        //when
        PersonType personType1=personTypeService.createPersonType(personType);

        //
        assertThat(personType1).isNotNull();
        assertThat(personType1.getId()).isEqualTo(1L);
        assertThat(personType1.getType()).isEqualTo("type1");
        assertThat(personType1.getName()).isEqualTo("name1");
        assertThat(personType1.getSurname()).isEqualTo("surname1");
        assertThat(personType1.getBussinessName()).isEqualTo("bussinessName");
    }
}
