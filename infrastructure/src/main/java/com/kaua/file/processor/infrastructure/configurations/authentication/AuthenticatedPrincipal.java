package com.kaua.file.processor.infrastructure.configurations.authentication;

public sealed interface AuthenticatedPrincipal permits AuthenticatedService, AuthenticatedUser {

    String id();
}
