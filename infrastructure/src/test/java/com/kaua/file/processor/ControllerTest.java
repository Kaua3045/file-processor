package com.kaua.file.processor;

import com.kaua.file.processor.infrastructure.configurations.OtelConfig;
import com.kaua.file.processor.infrastructure.configurations.SecurityConfig;
import com.kaua.file.processor.infrastructure.idempotency.gateways.InMemoryIdempotencyKeyGateway;
import com.kaua.file.processor.infrastructure.wrapper.OpenTelemetryMetrics;
import com.kaua.file.processor.infrastructure.wrapper.TracerWrapperOtel;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test-integration")
@WebMvcTest
@TestPropertySource(properties = "application.otel.memory-exporter=true")
@Import({SecurityConfig.class, IntegrationTestConfig.class, OtelConfig.class, InMemoryIdempotencyKeyGateway.class, ObservationTest.OpenTelemetryTestConfig.class, TracerWrapperOtel.class, OpenTelemetryMetrics.class})
@Tag("integrationTest")
public @interface ControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
