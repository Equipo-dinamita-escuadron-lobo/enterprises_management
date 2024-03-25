package com.enterprises_management;


import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.EnterpriseController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class EnterpriseControllerIntegrationTest {

    @Autowired
    EnterpriseController enterpriseController;

    @Test
    void contextLoads() throws Exception {
        assertThat(enterpriseController).isNotNull();
    }
}
