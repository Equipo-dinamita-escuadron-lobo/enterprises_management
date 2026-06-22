package com.enterprises_management.enterprise.application.ports.input;

import org.springframework.core.io.Resource;

public class LogoResource {

    private final Resource resource;
    private final String contentType;

    public LogoResource(Resource resource, String contentType) {
        this.resource = resource;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public String getContentType() {
        return contentType;
    }
}