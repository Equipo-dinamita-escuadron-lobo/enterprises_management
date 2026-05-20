package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

public class LogoNotFoundException extends RuntimeException {
    public LogoNotFoundException(String message) {
        super(message);
    }
}

class InvalidLogoException extends RuntimeException {
    public InvalidLogoException(String message) {
        super(message);
    }
}