package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection;

import java.util.UUID;

public interface IEnterpriseInfoProjection {
    UUID getId();
    String getName();
    String getNit();
    String getLogo();
}
