package com.altinntech.clicksave.annotations;

import com.altinntech.clicksave.enums.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Column} annotation is used to mark fields that should be represented in a table.
 * It provides options to specify the field type, whether it is an ID, or a primary key.
 *
 * @author Fyodor Plotnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {

    /**
     * Specifies the field type.
     *
     * @return the field type.
     */
    FieldType value();

    /**
     * Indicates if the field is an ID.
     *
     * @return {@code true} if the field is an ID, {@code false} otherwise.
     */
    boolean id() default false;

    /**
     * Indicates if the field is a primary key.
     *
     * @return {@code true} if the field is a primary key, {@code false} otherwise.
     */
    boolean primaryKey() default false;
}
