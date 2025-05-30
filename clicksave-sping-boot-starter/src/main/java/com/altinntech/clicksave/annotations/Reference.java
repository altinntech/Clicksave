package com.altinntech.clicksave.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Reference} annotation is used to help the ORM determine a field of an entity.
 * It is used in projections.
 *
 * @author Fyodor Plotnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Reference {

    /**
     * Specifies the value for the reference.
     *
     * @return the value for the reference.
     */
    String value();
}

