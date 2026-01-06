package com.kaua.file.processor.infrastructure.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.kaua.file.processor")
@EnableScheduling
public class WebServerConfig {
}
