package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Third;

import java.util.List;
import java.util.UUID;

public interface IThirdsApiOutputPort {
    String getThirdsByEnterprise(UUID entId, int numPage, int size, String sortField, String sortOrder);
}