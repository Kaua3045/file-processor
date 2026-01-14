package com.kaua.file.processor.infrastructure.processedEvents;

import com.kaua.file.processor.AbstractRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProcessedEventJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void assertDependencies() {
        Assertions.assertNotNull(processedEventRepository());
    }

    @Test
    void givenAValidEventId_whenCallsSave_shouldPersistIt() {
        Assertions.assertEquals(0, countProcessedEvents());

        processedEventRepository().save("event-123");

        Assertions.assertEquals(1, countProcessedEvents());
    }

    @Test
    void givenAnExistingEventId_whenCallsExistsById_shouldReturnTrue() {
        Assertions.assertEquals(0, countProcessedEvents());

        processedEventRepository().save("event-123");

        final var exists = processedEventRepository().existsById("event-123");

        Assertions.assertTrue(exists);

        Assertions.assertEquals(1, countProcessedEvents());
    }

    @Test
    void givenANonExistingEventId_whenCallsExistsById_shouldReturnFalse() {
        Assertions.assertEquals(0, countProcessedEvents());

        final var exists = processedEventRepository().existsById("event-999");

        Assertions.assertFalse(exists);
    }
}
