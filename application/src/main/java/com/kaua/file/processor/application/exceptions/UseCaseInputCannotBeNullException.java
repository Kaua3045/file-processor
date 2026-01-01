package com.kaua.file.processor.application.exceptions;

import com.kaua.file.processor.domain.exceptions.NoStackTraceException;

public class UseCaseInputCannotBeNullException extends NoStackTraceException {

    public UseCaseInputCannotBeNullException(String useCaseName) {
        super("Input to %s cannot be null".formatted(useCaseName));
    }
}
