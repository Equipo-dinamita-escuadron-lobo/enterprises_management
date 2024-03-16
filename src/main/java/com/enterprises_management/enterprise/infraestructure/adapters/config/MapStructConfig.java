package com.enterprises_management.enterprise.infraestructure.adapters.config;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.*;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.*;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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


    @Bean
    ICitiesbyDepartmentMapper mapStructMapperCitiesMapper(){return Mappers.getMapper( ICitiesbyDepartmentMapper.class);

    }
    @Bean
    ICitiesbyDepartmentRestMapper mapStructMapperCitiesRestMapper(){return Mappers.getMapper( ICitiesbyDepartmentRestMapper.class);
    }

    @Bean
    IDepartmentsMapper mapStructMapperDepartmetsMapper(){return Mappers.getMapper( IDepartmentsMapper.class);}
    @Bean
    IDepartmentRestMapper mapStructMapperDepartmetRestMappper(){return Mappers.getMapper( IDepartmentRestMapper.class); }
}

