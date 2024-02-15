package com.altinntech.clicksave.annotations;


import com.altinntech.clicksave.enums.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {
    FieldType value() default FieldType.NONE;
    boolean id() default false;
    boolean primaryKey() default false;
}
