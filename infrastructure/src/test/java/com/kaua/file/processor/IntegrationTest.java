package com.kaua.file.processor;

import com.kaua.file.processor.application.wrapper.Metrics;
import com.kaua.file.processor.infrastructure.configurations.WebServerConfig;
import com.kaua.file.processor.infrastructure.wrapper.OpenTelemetryMetrics;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test-integration")
@SpringBootTest(classes = {
        WebServerConfig.class,
        IntegrationTestConfig.class,
        ObservationTest.OpenTelemetryTestConfig.class,
        OpenTelemetryMetrics.class
})
@Tag("integrationTest")
public @interface IntegrationTest {
}
