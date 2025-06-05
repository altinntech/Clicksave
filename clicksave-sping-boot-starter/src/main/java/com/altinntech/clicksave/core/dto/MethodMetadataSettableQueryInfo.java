package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Query;
import com.altinntech.clicksave.annotations.SettableQuery;
import com.altinntech.clicksave.interfaces.ClickHouseJpa;
import com.altinntech.clicksave.interfaces.QueryInfo;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class MethodMetadataSettableQueryInfo implements QueryInfo {

    private final MethodMetadata methodMetadata;
    private final Class<?> returnType;
    private final String queryString;
    private final List<Object> args;

    public MethodMetadataSettableQueryInfo(MethodMetadata methodMetadata, Class<?> returnType, String queryString, List args) {
        this.methodMetadata = methodMetadata;
        this.returnType = returnType;
        this.queryString = queryString;
        this.args = args;
    }

    @Override
    public String methodName() {
        return methodMetadata.getSourceMethod().getName();
    }

    @Override
    public Class<?> returnClass() {
        return returnType;
    }

    @Override
    public Class<?> entityClass() {
        return entityType(methodMetadata);
    }

    @Override
    public Class<?> containerClass() {
        Type returnType = methodMetadata.getReturnTypeMetadata().getResolvedType();
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                return rawClass;
            }
        } else if (returnType instanceof Class<?>) {
            return null;
        }
        return null;
    }

    @Override
    public String queryString() {
        return queryString;
    }

    @Override
    public List<Object> args() {
        return args;
    }

    @Override
    public boolean isParsedByMethodName() {
        return false;
    }

    private Class<?> entityType(MethodMetadata methodMetadata) {
        Class<?> sourceClass = methodMetadata.getSourceClassMetadata().getSourceClass();
        Type[] interfaces = sourceClass.getGenericInterfaces();

        for (Type type : interfaces) {
            if (type instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType.getTypeName().equals(ClickHouseJpa.class.getName())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    return (Class<?>) typeArguments[0];
                }
            }
        }
        return null;
    }
}
