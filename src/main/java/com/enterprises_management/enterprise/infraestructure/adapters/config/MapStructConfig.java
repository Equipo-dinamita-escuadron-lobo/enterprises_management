package com.enterprises_management.enterprise.infraestructure.adapters.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.IEnterpriseRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.ITaxLiabilityRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ITaxLiabilityMapper;


@Configuration
public class MapStructConfig {
    
    @Bean
    public ITaxLiabilityMapper mapStructMapper() {
        return Mappers.getMapper(ITaxLiabilityMapper.class);
    }
    
    @Bean
    public ITaxLiabilityRestMapper mapStructMapperRest() {
        return Mappers.getMapper(ITaxLiabilityRestMapper.class);
    }

    @Bean 
    IEnterpriseMapper mapStructMapperEnterprise() {
        return Mappers.getMapper(IEnterpriseMapper.class);
    }

    @Bean
    IEnterpriseRestMapper mapStructMapperEnterpriseRest() {
        return Mappers.getMapper(IEnterpriseRestMapper.class);
    }
}

