package com.enterprises_management.enterprise.domain.enums;

import lombok.Getter;

@Getter
public enum StateEnum {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido");

    private final String state;

    StateEnum(String state) {
        this.state = state;
    }
}

