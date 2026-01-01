package com.kaua.file.processor.domain.exceptions;

import com.kaua.file.processor.domain.AggregateRoot;
import com.kaua.file.processor.domain.Identifier;
import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.domain.validation.ValidationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class NotFoundExceptionTest extends UnitTest {

    @Test
    void givenAValidAggregateWithIdentifierFieldName_whenCallNotFoundExceptionWith_ThenReturnNotFoundException() {
        // given
        final var aggregate = SampleAggregate.class;
        final var aIdentifierField = "id";
        final var aId = "123";
        final var expectedErrorMessage = "SampleAggregate with id 123 was not found";

        // when
        final var notFoundException = NotFoundException.with(aggregate, aIdentifierField, aId);
        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.get().getMessage());
    }

    @Test
    void givenAValidAggregateStringNameWithIdentifierFieldName_whenCallNotFoundExceptionWith_ThenReturnNotFoundException() {
        // given
        final var aggregate = "SampleAggregate";
        final var aIdentifierField = "id";
        final var aId = "123";
        final var expectedErrorMessage = "SampleAggregate with id 123 was not found";

        // when
        final var notFoundException = NotFoundException.with(aggregate, aIdentifierField, aId);
        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.get().getMessage());
    }

    @Test
    void givenAValidAggregateUUIDWithIdentifierFieldName_whenCallNotFoundExceptionWith_ThenReturnNotFoundException() {
        // given
        final var aggregate = SampleAggregate.class;
        final var aIdentifierField = "id";
        final var aId = new SampleUUIDIdentifier(UUID.randomUUID());
        final var expectedErrorMessage = "SampleAggregate with id %s was not found"
                .formatted(aId.value());

        // when
        final var notFoundException = NotFoundException.with(aggregate, aIdentifierField, aId);

        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.get().getMessage());
    }

    @Test
    void givenAValidAggregateUUIDStringName_whenCallNotFoundExceptionWith_ThenReturnNotFoundException() {
        // given
        final var aggregate = SampleAggregate.class;
        final var aId = new SampleUUIDIdentifier(UUID.randomUUID());
        final var expectedErrorMessage = "SampleAggregate with id %s was not found"
                .formatted(aId.value());

        // when
        final var notFoundException = NotFoundException.with(aggregate, aId);

        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.get().getMessage());
    }

    @Test
    void givenAValidAggregateId_whenCallNotFoundExceptionWith_ThenReturnNotFoundException() {
        // given
        final var aggregate = "SampleAggregate";
        final var aId = IdentifierUtils.generateNewId();
        final var expectedErrorMessage = "SampleAggregate with id %s was not found"
                .formatted(aId);

        // when
        final var notFoundException = NotFoundException.with(aggregate, aId);

        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.get().getMessage());
    }

    @Test
    void givenAValidAggregateId_whenCallNotFoundExceptionWithAggregate_ThenReturnNotFoundException() {
        // given
        final var aggregate = SampleAggregate.class;
        final var aId = IdentifierUtils.generateNewId();
        final var expectedErrorMessage = "SampleAggregate with id %s was not found"
                .formatted(aId);

        // when
        final var notFoundException = NotFoundException.with(aggregate, aId);

        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.get().getMessage());
    }

    @Test
    void givenAValidMessage_whenCallNotFoundExceptionWithMessage_ThenReturnNotFoundException() {
        // given
        final var message = "Sample message";
        final var expectedErrorMessage = "Sample message";

        // when
        final var notFoundException = NotFoundException.with(message);

        // then
        Assertions.assertEquals(expectedErrorMessage, notFoundException.getMessage());
    }

    private static class SampleAggregate extends AggregateRoot<SampleIdentifier> {
        public SampleAggregate(SampleIdentifier id) {
            super(id);
        }

        @Override
        public void validate(ValidationHandler aHandler) {

        }
    }

    private record SampleIdentifier(String value) implements Identifier<String> {
    }

    private record SampleUUIDIdentifier(UUID value) implements Identifier<UUID> {
    }
}
