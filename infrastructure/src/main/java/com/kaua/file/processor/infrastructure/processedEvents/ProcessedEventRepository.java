package com.kaua.file.processor.infrastructure.processedEvents;

public interface ProcessedEventRepository {

    void save(String eventId);

    boolean existsById(String eventId);
}
