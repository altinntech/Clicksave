package com.altinntech.clicksave.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Batching} annotation is used to specify batching settings for save operations.
 * It allows configuring the batch size for efficient saving of entities.
 *
 * @author Fyodor Plotnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Batching {

    /**
     * Specifies the batch size for saving operations.
     *
     * @return the batch size for saving operations.
     */
    int batchSize();
}
