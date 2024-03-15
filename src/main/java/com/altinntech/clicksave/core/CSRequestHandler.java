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

import static com.altinntech.clicksave.log.CSLogger.error;

/**
 * The {@code CSRequestHandler} class intercepts requests to methods in the repository.
 * It handles various CRUD operations and query executions.
 *
 * <p>This class is annotated with {@code @Component} for Spring dependency injection.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
@Component
public class CSRequestHandler implements MethodHandler {

    private final CHRepository repository;
    private final QueryExecutor queryExecutor;
    private final CSBootstrap bootstrap;

    /**
     * Constructs a new CSRequestHandler instance.
     *
     * @param queryExecutor the query executor
     * @param bootstrap the bootstrap
     */
    @Autowired
    public CSRequestHandler(QueryExecutor queryExecutor, CSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.repository = CHRepository.getInstance();
        this.queryExecutor = queryExecutor;
    }

    @Override
    public Object handle(Object[] arguments, MethodMetadata methodMetadata) {
        Class<?> entityType = findEntityType(methodMetadata);
        Class<?> entityIdType = findEntityIdType(methodMetadata);
        Class<?> methodReturnType = getParameterType(methodMetadata);

        try {
            switch (methodMetadata.getSourceMethod().getName()) {
                case "save" -> {
                    return handleSave(arguments, entityIdType);
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
                    return handleQuery(methodReturnType, entityType, arguments, methodMetadata);
                }
            }
        } catch (SQLException e) {
            error("Error while processing SQL query: " + e.getMessage(), this.getClass());
        } catch (ClassCacheNotFoundException e) {
            error("ClassDataCache not found for " + entityType + ". Make sure that entity has an @ClickHouseEntity annotation", this.getClass());
        } catch (IllegalAccessException e) {
            error("Illegal access to entity " + entityType + ": " + e.getMessage(), this.getClass());
        }
        return null;
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

    private Class<?> findEntityIdType(MethodMetadata methodMetadata) {
        Class<?> sourceClass = methodMetadata.getSourceClassMetadata().getSourceClass();
        Type[] interfaces = sourceClass.getGenericInterfaces();

        for (Type type : interfaces) {
            if (type instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType.getTypeName().equals(ClickHouseJpa.class.getName())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    return (Class<?>) typeArguments[1];
                }
            }
        }
        return null;
    }

    private static Class<?> getParameterType(MethodMetadata methodMetadata) {
        Type returnType = methodMetadata.getReturnTypeMetadata().getResolvedType();
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> parameterType) {
                return parameterType;
            }
        }
        return null;
    }

    private Object handleFindAll(Class<?> entityType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        return repository.findAll(entityType);
    }

    private Object handleSave(Object[] arguments, Class<?> entityIdType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        return repository.save(arguments[0], entityIdType);
    }

    private Object handleFindById(Class<?> entityType, Object[] arguments) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        return Optional.ofNullable(repository.findById(entityType, arguments[0]));
    }

    private void handleDelete(Object argument) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        repository.delete(argument);
    }

    private void handleDeleteAll(Class<?> entityType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        repository.deleteAll(entityType);
    }

    private Object handleQuery(Class<?> methodReturnType, Class<?> entityType, Object[] arguments, MethodMetadata methodMetadata) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        return queryExecutor.processQuery(methodReturnType, entityType, arguments, methodMetadata);
    }
}
