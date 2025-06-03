package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.interfaces.ClicksaveMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class MicrometerMetrics implements ClicksaveMetrics {

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementCounter(String name) {
        meterRegistry.counter(micrometerName(name)).increment();
    }

    @Override
    public void incrementCounter(String name, int value) {
        meterRegistry.counter(micrometerName(name)).increment(value);
    }

    @Override
    public void registerNumValueCheck(String name, Supplier<Number> source) {
        meterRegistry.gauge(micrometerName(name), this, o -> source.get().doubleValue());
    }

    private String micrometerName(String name) {
        return String.format("com.altinntech.clicksave.%s", name);
    }
}
