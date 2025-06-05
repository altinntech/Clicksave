package com.altinntech.clicksave.annotations;

import com.altinntech.clicksave.enums.EnumType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code EnumColumn} annotation is used to mark fields represented by an enumeration.
 * It allows specifying the type of the enumeration.
 *
 * @author Fyodor Plotnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EnumColumn {

    /**
     * Specifies the type of the enumeration.
     *
     * @return the enumeration type.
     */
    EnumType value() default EnumType.STRING;

    /**
     * Specifies nullable option
     */
    boolean nullable() default false;
}

