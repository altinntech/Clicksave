package com.altinntech.clicksave.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code ClickHouseEntity} annotation is used to mark a class as a ClickHouse entity.
 * It is intended to be used to identify classes that represent entities in ClickHouse database.
 *
 * @author Fyodor Plotnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClickHouseEntity {
    boolean forTest() default false;
}
