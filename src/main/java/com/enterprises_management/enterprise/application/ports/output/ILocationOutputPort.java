package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Location;

public interface ILocationOutputPort {
    Location create(Location location);
    boolean delete(Long id);
}
