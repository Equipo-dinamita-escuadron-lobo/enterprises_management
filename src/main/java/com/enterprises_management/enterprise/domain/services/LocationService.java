package com.enterprises_management.enterprise.domain.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ILocationMangerPort;
import com.enterprises_management.enterprise.application.ports.output.ILocationOutputPort;
import com.enterprises_management.enterprise.domain.models.Location;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LocationService implements ILocationMangerPort {

    private final ILocationOutputPort locationOutputPort;

    @Override
    public Location createLocation(Location location) {
        return locationOutputPort.create(location);
    }
    
}
