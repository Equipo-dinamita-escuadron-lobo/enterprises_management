package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.validation.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.validation.interfaces.IUniqueNit;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueNitValidator implements ConstraintValidator<IUniqueNit, String>{

    @Autowired
    private IEnterpriseRepository enterpriseRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
       return !enterpriseRepository.existsByNit(value);
    }
    
}
