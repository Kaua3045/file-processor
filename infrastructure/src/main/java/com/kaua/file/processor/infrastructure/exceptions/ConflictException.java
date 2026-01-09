package com.kaua.file.processor.infrastructure.exceptions;

import com.kaua.file.processor.domain.exceptions.NoStackTraceException;
import com.kaua.file.processor.domain.validation.Error;

import java.util.Collections;
import java.util.List;

public class ConflictException extends NoStackTraceException {

    private final List<Error> errors;

    private ConflictException(final String message, final List<Error> errors) {
        super(message);
        this.errors = errors;
    }

    public static ConflictException with(final String message) {
        return new ConflictException(message, Collections.emptyList());
    }

    public static ConflictException with(final List<Error> errors) {
        return new ConflictException("ConflictException", errors);
    }

    public List<Error> getErrors() {
        return errors;
    }
}
