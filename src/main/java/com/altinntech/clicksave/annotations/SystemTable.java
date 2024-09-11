package com.altinntech.clicksave.annotations;

import com.altinntech.clicksave.enums.EngineType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SystemTable {

    EngineType engine() default EngineType.MergeTree;
}
