package com.enterprises_management.enterprise.infraestructure.adapters.config;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IAddressRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ITaxLiabilityRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IAddressMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseCreateMapper;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ILocationMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IPersonTypeMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ITaxLiabilityMapper;


@Configuration
public class MapStructConfig {
    
    @Bean
    ITaxLiabilityMapper mapStructMapper() {return Mappers.getMapper(ITaxLiabilityMapper.class);}
    
    @Bean
    ITaxLiabilityRestMapper mapStructMapperRest() {return Mappers.getMapper(ITaxLiabilityRestMapper.class);}

    @Bean 
    IEnterpriseSearchMapper mapStructMapperEnterprise() {return Mappers.getMapper(IEnterpriseSearchMapper.class);}

    @Bean
    IEnterpriseRestMapper mapStructMapperEnterpriseRest() {return Mappers.getMapper(IEnterpriseRestMapper.class);}
   
    @Bean
    IAddressMapper mapStructMapperAddress(){return Mappers.getMapper(IAddressMapper.class);}
    
    @Bean
    IAddressRestMapper mapStructMapperAddressRestMapper(){return Mappers.getMapper(IAddressRestMapper.class);}

    @Bean
    IEnterpriseCreateMapper mapStructMapperEnterpriseCreate(){return Mappers.getMapper(IEnterpriseCreateMapper.class);}

    @Bean
    ILocationMapper mapStructMapperLocation(){return Mappers.getMapper(ILocationMapper.class);}

    @Bean
    IPersonTypeMapper mapStructMapperPersonType(){return Mappers.getMapper(IPersonTypeMapper.class);}
}

