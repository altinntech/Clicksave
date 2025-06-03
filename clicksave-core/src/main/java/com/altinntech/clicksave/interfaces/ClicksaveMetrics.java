package com.altinntech.clicksave.interfaces;

import java.util.function.Supplier;

public interface ClicksaveMetrics {

    /**
     *
     TODO Micrometer impl



     this.successBatchCount = meterRegistry.counter("clicksave.batch.success_save_count", "batch", "save");
     this.failedBatchCount = meterRegistry.counter("clicksave.batch.failed_save_count", "batch", "save");

     * @param name
     */

    void incrementCounter(String name);
    void incrementCounter(String name, int value);
    void registerNumValueCheck(String name, Supplier<Number> source);

    static ClicksaveMetrics noop() {
        return new NoOp();
    }

    class NoOp implements ClicksaveMetrics {

        @Override
        public void incrementCounter(String name) {

        }

        @Override
        public void incrementCounter(String name, int value) {

        }

        @Override
        public void registerNumValueCheck(String name, Supplier<Number> source) {

        }
    }

}
