package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.query.executor.QueryExecutor;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;
import com.altinntech.clicksave.interfaces.ClickHouseJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thepavel.icomponent.handler.MethodHandler;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.altinntech.clicksave.log.CSLogger.error;
import static com.altinntech.clicksave.log.CSLogger.info;

@Component
public class CSRequestHandler implements MethodHandler {

    private final CHRepository repository;
    private final QueryExecutor queryExecutor;

    @Autowired
    public CSRequestHandler(CHRepository repository, QueryExecutor queryExecutor) {
        this.repository = repository;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public Object handle(Object[] arguments, MethodMetadata methodMetadata) {
        Class<?> entityType = findEntityType(methodMetadata);

        switch (methodMetadata.getSourceMethod().getName()) {
            case "save" -> {
                return handleSave(arguments);
            }
            case "findById" -> {
                return handleFindById(entityType, arguments);
            }
            case "findAll" -> {
                return handleFindAll(entityType);
            }
            case "delete" -> handleDelete(arguments[0]);
            case "deleteAll" -> handleDeleteAll(entityType);
            default -> {
                return handleQuery(entityType, arguments, methodMetadata);
            }
        }
        return null;
    }

    private Object handleFindAll(Class<?> entityType) {
        try {
            return repository.findAll(entityType);
        } catch (ClassCacheNotFoundException | SQLException e) {
            error(e.getMessage());
        }
        return new ArrayList<>();
    }

    private Class<?> findEntityType(MethodMetadata methodMetadata) {
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

    private Object handleSave(Object[] arguments) {
        try {
            return repository.save(arguments[0]);
        } catch (FieldInitializationException | ClassCacheNotFoundException | SQLException | IllegalAccessException e) {
            error(e.getMessage());
        }
        return null;
    }

    private Object handleFindById(Class<?> entityType, Object[] arguments) {
        try {
            return Optional.ofNullable(repository.findById(entityType, (UUID) arguments[0]));
        } catch (ClassCacheNotFoundException e) {
            error(e.getMessage());
        }
        return Optional.empty();
    }

    private void handleDelete(Object argument) {
        try {
            repository.delete(argument);
        } catch (ClassCacheNotFoundException | IllegalAccessException e) {
            error(e.getMessage());
        }
    }

    private void handleDeleteAll(Class<?> entityType) {
        try {
            repository.deleteAll(entityType);
        } catch (ClassCacheNotFoundException e) {
            error(e.getMessage());
        }
    }

    private Object handleQuery(Class<?> entityType, Object[] arguments, MethodMetadata methodMetadata) {
        try {
            return queryExecutor.processQuery(entityType, arguments, methodMetadata);
        } catch (ClassCacheNotFoundException | SQLException e) {
            error(e.getMessage());
        }
        return null;
    }
}
