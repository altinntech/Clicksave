package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.interfaces.QueryInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

public record SimpleQueryInfo(
        String methodName,
        Class<?> returnClass,
        Class<?> entityClass,
        String queryString,
        List<Object> args,
        boolean isParsedByMethodName
) implements QueryInfo {}
