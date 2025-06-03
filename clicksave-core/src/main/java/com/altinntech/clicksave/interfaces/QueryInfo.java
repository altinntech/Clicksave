package com.altinntech.clicksave.interfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public interface QueryInfo {

    String methodName();
    Class<?> returnClass();
    Class<?> entityClass();
    Class<?> containerClass();
    String queryString();
    List<Object> args();
    boolean isParsedByMethodName();

    default String queryId() {
        return String.format(
                "%s_%s_%s_%d",
                methodName(), entityClass().getSimpleName(), returnClass().getSimpleName(),
                Optional.ofNullable(queryString()).map(Object::hashCode).orElse(0)
        );
    }
}
