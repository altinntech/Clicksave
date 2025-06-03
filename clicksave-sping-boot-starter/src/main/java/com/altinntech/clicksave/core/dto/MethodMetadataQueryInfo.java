package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Query;
import com.altinntech.clicksave.annotations.SettableQuery;
import com.altinntech.clicksave.core.query.builder.QueryPullType;
import com.altinntech.clicksave.interfaces.ClickHouseJpa;
import com.altinntech.clicksave.interfaces.QueryInfo;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MethodMetadataQueryInfo implements QueryInfo {

    private final MethodMetadata methodMetadata;
    private final List<Object> args;

    public MethodMetadataQueryInfo(MethodMetadata methodMetadata, Object ... args) {
        this.methodMetadata = methodMetadata;
        this.args = Arrays.asList(args);
    }

    @Override
    public String methodName() {
        return methodMetadata.getSourceMethod().getName();
    }

    @Override
    public Class<?> returnClass() {
        return returnType(methodMetadata);
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
        Query query = query();
        if (query != null) {
            return query.value();
        } else if (settableQuery() != null) {
            return args.get(1).toString();
        }
        return "";
    }

    @Override
    public List<Object> args() {
        SettableQuery settableQuery = settableQuery();
        if (settableQuery != null) {
            return args.subList(2, args.size());
        } else {
            return args;
        }
    }

    @Override
    public boolean isParsedByMethodName() {
        return queryString() == null;
    }

    private Query query() {
        return methodMetadata.getSourceMethod().getAnnotation(Query.class);
    }

    private SettableQuery settableQuery() {
        return methodMetadata.getSourceMethod().getAnnotation(SettableQuery.class);
    }

    private Class<?> returnType(MethodMetadata methodMetadata) {
        Type returnType = methodMetadata.getReturnTypeMetadata().getResolvedType();
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> parameterType) {
                return parameterType;
            }
        }
        return null;
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
