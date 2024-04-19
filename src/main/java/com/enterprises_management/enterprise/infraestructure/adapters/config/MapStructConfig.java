package com.enterprises_management.enterprise.infraestructure.adapters.config;


import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.*;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ICitiesbyDepartmentRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IDepartmentRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ITaxLiabilityRestMapper;
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
    ICitiesbyDepartmentMapper mapStructMapperCitiesMapper(){return Mappers.getMapper( ICitiesbyDepartmentMapper.class);

    }
    @Bean
    ICitiesbyDepartmentRestMapper mapStructMapperCitiesRestMapper(){return Mappers.getMapper( ICitiesbyDepartmentRestMapper.class);
    }

    @Bean
    IDepartmentsMapper mapStructMapperDepartmetsMapper(){return Mappers.getMapper( IDepartmentsMapper.class);}
    @Bean
    IDepartmentRestMapper mapStructMapperDepartmetRestMappper(){return Mappers.getMapper( IDepartmentRestMapper.class); }

   


    @Bean
    IEnterpriseCreateMapper mapStructMapperEnterpriseCreate(){return Mappers.getMapper(IEnterpriseCreateMapper.class);}

    @Bean
    ILocationMapper mapStructMapperLocation(){return Mappers.getMapper(ILocationMapper.class);}

    @Bean
    IPersonTypeMapper mapStructMapperPersonType(){return Mappers.getMapper(IPersonTypeMapper.class);}


    @Bean
    IEnterpriseUpdateMapper mapStructMapperEnterpriseUpdate(){return Mappers.getMapper(IEnterpriseUpdateMapper.class);}
}

