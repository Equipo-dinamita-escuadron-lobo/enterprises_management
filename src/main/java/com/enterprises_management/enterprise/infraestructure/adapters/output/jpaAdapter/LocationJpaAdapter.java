package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ILocationOutputPort;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ILocationMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ILocationRepository;

import lombok.Data;

@Component
@Data
public class LocationJpaAdapter implements ILocationOutputPort {

    private final ILocationRepository locationRepository;
    private final ILocationMapper locationMapper;

    @Override
    public Location create(Location location) {
        LocationEntity locationEntity = locationMapper.toEntity(location);
        if (locationEntity.getId() != null) {return null;}
        
        return locationMapper.toDomain(locationRepository.save(locationEntity));     
    }
    
}
