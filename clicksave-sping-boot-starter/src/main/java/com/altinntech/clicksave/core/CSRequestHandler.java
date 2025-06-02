package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.MethodMetadataQueryInfo;
import com.altinntech.clicksave.core.query.executor.QueryExecutor;
import com.altinntech.clicksave.exceptions.*;
import com.altinntech.clicksave.interfaces.ClickHouseJpa;
import com.altinntech.clicksave.log.CSLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thepavel.icomponent.handler.MethodHandler;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.Future;

import static com.altinntech.clicksave.log.CSLogger.error;

/**
 * The {@code CSRequestHandler} class intercepts requests to methods in the repository.
 * It handles various CRUD operations and query executions.
 *
 * <p>This class is annotated with {@code @Component} for Spring dependency injection.</p>
 *
 * @author Fyodor Plotnikov
 */
@Component
public class CSRequestHandler implements MethodHandler {

    private final ClicksaveInternalRepository repository;
    private final QueryExecutor queryExecutor;
    private final CSBootstrap bootstrap;
    private final ThreadPoolManager threadPoolManager;

    /**
     * Constructs a new CSRequestHandler instance.
     *
     * @param bootstrap the bootstrap
     */
    @Autowired
    public CSRequestHandler(CSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.repository = bootstrap.getRepository();
        this.queryExecutor = bootstrap.getQueryExecutor();
        this.threadPoolManager = bootstrap.getThreadPoolManager();
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
                case "saveAsync" -> {
                    return handleSaveAsync(arguments, entityIdType);
                }
                case "findById" -> {
                    return handleFindById(entityType, arguments);
                }
                case "findAll" -> {
                    return handleFindAll(entityType);
                }
                case "count" -> {
                    return handleCount(entityType);
                }
                case "delete" -> handleDelete(arguments[0]);
                case "deleteAll" -> handleDeleteAll(entityType);
                case "findAllCustomQuery" -> {
                    return handleCustomQuery(methodReturnType, entityType, arguments, methodMetadata);
                }
                case "saveBatch" -> {
                    handleSaveBatch(entityType);
                }
                default -> {
                    return handleQuery(methodReturnType, entityType, arguments, methodMetadata);
                }
            }
        } catch (SQLException e) {
            CSLogger.error("Error while processing SQL query: " + e.getMessage(), this.getClass());
            throw new ClicksaveSQLException(e);
        } catch (ClassCacheNotFoundException e) {
            CSLogger.error("ClassDataCache not found for " + entityType + ". Make sure that entity has an @ClickHouseEntity annotation", this.getClass());
            throw new ClassCacheNotFoundRuntimeException(e);
        } catch (IllegalAccessException e) {
            CSLogger.error("Illegal access to entity " + entityType + ": " + e.getMessage(), this.getClass());
            throw new ReflectiveException(e);
        } catch (InvocationTargetException e) {
            CSLogger.error("Illegal invocation method; Entity: " + entityType + ": " + e.getMessage(), this.getClass());
            throw new ReflectiveException(e);
        } catch (InterruptedException e) {
            throw new ConcurrencyException(e);
        } catch (Exception e) {
            throw new UnknownException(e);
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

    private Object handleFindAll(Class<?> entityType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        return repository.findAll(entityType);
    }

    private long handleCount(Class<?> entityType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        return repository.count(entityType);
    }

    private Object handleSave(Object[] arguments, Class<?> entityIdType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        return repository.save(arguments[0], entityIdType);
    }

    private <T> Future<T> handleSaveAsync(Object[] arguments, Class<T> entityIdType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException, InterruptedException {
        Future<T> future = threadPoolManager.saveAsync(arguments, entityIdType, repository);
        return future;
    }

    private Object handleFindById(Class<?> entityType, Object[] arguments) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        return Optional.ofNullable(repository.findById(entityType, arguments[0]));
    }

    private void handleDelete(Object argument) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        repository.delete(argument);
    }

    private void handleDeleteAll(Class<?> entityType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        repository.deleteAll(entityType);
    }

    private Object handleCustomQuery(Class<?> methodReturnType, Class<?> entityType, Object[] arguments, MethodMetadata methodMetadata) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        methodReturnType = (Class<?>) arguments[0];
        return queryExecutor.processQuery(methodReturnType, entityType, arguments, new MethodMetadataQueryInfo(methodMetadata));
    }

    private Object handleQuery(Class<?> methodReturnType, Class<?> entityType, Object[] arguments, MethodMetadata methodMetadata) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        return queryExecutor.processQuery(methodReturnType, entityType, arguments, new MethodMetadataQueryInfo(methodMetadata));
    }

    private void handleSaveBatch(Class<?> entityType) throws SQLException, ClassCacheNotFoundException, InvocationTargetException, IllegalAccessException {
        repository.saveBatch(entityType);
    }
}
