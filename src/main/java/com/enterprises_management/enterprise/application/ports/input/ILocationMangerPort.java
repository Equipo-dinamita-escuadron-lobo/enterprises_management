package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.Location;

public interface ILocationMangerPort {
    Location createLocation(Location location);
    boolean deleteLocation(Long id);
}
