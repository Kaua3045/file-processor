package com.kaua.file.processor.application;

import com.kaua.file.processor.application.wrapper.Metrics;

public class NoOpMetrics implements Metrics {

    @Override
    public void incrementCounter(String name, long value) {

    }

    @Override
    public void recordTime(String name, long milliseconds) {

    }

    @Override
    public void gauge(String name, long value) {

    }
}
