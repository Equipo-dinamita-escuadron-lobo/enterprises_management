package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;

public record ShareEnterpriseResponse(List<String> notified, List<String> rejected, List<String> pendingRegistration) {}
