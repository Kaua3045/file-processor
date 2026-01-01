package com.kaua.file.processor.infrastructure.configurations.authentication;

public record AuthenticatedUser(String id) implements AuthenticatedPrincipal {
}
