package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.validation.interfaces;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.validation.impl.UniqueNitValidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueNitValidator.class)
@Documented
public @interface IUniqueNit {
    String message() default "El NIT ya está en uso";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}