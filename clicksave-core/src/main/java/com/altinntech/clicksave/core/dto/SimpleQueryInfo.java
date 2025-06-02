package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.interfaces.QueryInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public record SimpleQueryInfo(
        String queryId,
        Type returnType,
        Annotation annotation
) implements QueryInfo {}
