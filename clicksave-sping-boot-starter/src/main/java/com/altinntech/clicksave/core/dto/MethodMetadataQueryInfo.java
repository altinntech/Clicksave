package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Query;
import com.altinntech.clicksave.annotations.SettableQuery;
import com.altinntech.clicksave.interfaces.QueryInfo;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class MethodMetadataQueryInfo implements QueryInfo {

    private final String queryId;
    private final Type returnType;
    private Annotation annotation;

    public MethodMetadataQueryInfo(MethodMetadata methodMetadata) {
        this.queryId = methodMetadata.getSourceMethod().getName();
        this.returnType = methodMetadata.getReturnTypeMetadata().getResolvedType();
        Annotation[] annotations = methodMetadata.getSourceMethod().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Query.class) {
                this.annotation = annotation;
                break;
            } else if (annotation.annotationType() == SettableQuery.class) {
                this.annotation = annotation;
                break;
            }
        }
    }

    @Override
    public String queryId() {
        return queryId;
    }

    @Override
    public Type returnType() {
        return returnType;
    }

    @Override
    public Annotation annotation() {
        return annotation;
    }
}
