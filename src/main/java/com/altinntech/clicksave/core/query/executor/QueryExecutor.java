package com.altinntech.clicksave.core.query.executor;

import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.annotations.Query;
import com.altinntech.clicksave.core.BatchCollector;
import com.altinntech.clicksave.core.CSBootstrap;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.core.caches.ProjectionClassDataCache;
import com.altinntech.clicksave.core.caches.QueryMetadataCache;
import com.altinntech.clicksave.core.dto.*;
import com.altinntech.clicksave.core.query.builder.QueryBuilder;
import com.altinntech.clicksave.core.query.builder.QueryPullType;
import com.altinntech.clicksave.core.query.parser.Part;
import com.altinntech.clicksave.core.query.parser.PartParser;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.EnumId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@code QueryExecutor} class is responsible for executing assembled queries.
 * It processes queries and returns objects or lists of objects from the database.
 *
 * <p>This class is annotated with {@code @Component} for Spring dependency injection.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
@Component
public class QueryExecutor {

    private final CSBootstrap bootstrap;
    private final BatchCollector batchCollector;
    private final QueryMetadataCache queryMetadataCache = QueryMetadataCache.getInstance();
    private final ProjectionClassDataCache projectionClassDataCache = ProjectionClassDataCache.getInstance();

    /**
     * Constructs a new QueryExecutor instance.
     *
     * @param bootstrap the bootstrap
     */
    @Autowired
    public QueryExecutor(CSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.batchCollector = bootstrap.getBatchCollector();
    }

    /**
     * Processes the query and returns the result object.
     *
     * @param returnClass    the return class
     * @param entityClass    the entity class
     * @param arguments      the arguments
     * @param methodMetadata the method metadata
     * @return the result object
     * @throws ClassCacheNotFoundException if class cache is not found
     * @throws SQLException                if an SQL exception occurs
     */
    public Object processQuery(Class<?> returnClass, Class<?> entityClass, Object[] arguments, MethodMetadata methodMetadata) throws ClassCacheNotFoundException, SQLException, IllegalAccessException {
        String methodName = methodMetadata.getSourceMethod().getName();
        ClassDataCache classDataCache = bootstrap.getClassDataCache(entityClass);
        batchCollector.saveAndFlush(classDataCache);

        if (!queryMetadataCache.containsKey(methodName)) {
            Annotation[] annotations = methodMetadata.getSourceMethod().getAnnotations();
            Query queryAnnotation = null;
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Query.class) {
                    queryAnnotation = (Query) annotation;
                    break;
                }
            }

            if (queryAnnotation != null) {
                CustomQueryMetadata customQueryMetadata = new CustomQueryMetadata();
                customQueryMetadata.setQueryBody(queryAnnotation.value());
                customQueryMetadata.setPullType(getPullType(methodMetadata));
                customQueryMetadata.setIsQueryFromAnnotation(true);
                queryMetadataCache.addToCache(methodName, customQueryMetadata);
            } else {
                parseQueryMethod(returnClass, entityClass, methodName, classDataCache, methodMetadata);
            }
        }

        CustomQueryMetadata query = (CustomQueryMetadata) queryMetadataCache.getFromCache(methodName);
        try(Connection connection = bootstrap.getConnection();
            PreparedStatement statement = connection.prepareStatement(query.getQueryBody())) {
            if (query.getIsQueryFromAnnotation()) {
                for (int i = 1; i < arguments.length + 1; i++) {
                    statement.setObject(i, arguments[i - 1]);
                }
            } else {
                for (int i = 1; i < arguments.length + 1; i++) {
                    setStatementArgument(arguments, query, statement, i);
                }
            }

            switch (query.getPullType()) {
                case SINGLE -> {

                    try(ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Object entity;
                            if (!returnClass.equals(entityClass))
                                entity = CSUtils.createDtoEntityFromResultSet(returnClass, resultSet, classDataCache);
                            else
                                entity = CSUtils.createEntityFromResultSet(entityClass, resultSet, classDataCache);
                            bootstrap.releaseConnection(connection);
                            return Optional.ofNullable(entity);
                        } else {
                            return Optional.empty();
                        }
                    }

                }
                case MULTIPLE -> {

                    try(ResultSet resultSet = statement.executeQuery()) {
                        List<Object> entities = new ArrayList<>();
                        while (resultSet.next()) {
                            Object entity;
                            if (!returnClass.equals(entityClass))
                                entity = CSUtils.createDtoEntityFromResultSet(returnClass, resultSet, classDataCache);
                            else
                                entity = CSUtils.createEntityFromResultSet(entityClass, resultSet, classDataCache);
                            entities.add(entity);
                        }
                        bootstrap.releaseConnection(connection);
                        return entities;
                    }

                }
            }
        }

        return null;
    }

    private static void setStatementArgument(Object[] arguments, CustomQueryMetadata query, PreparedStatement statement, int i) throws SQLException {
        FieldDataCache currentFieldData = query.getFields().get(i - 1);
        Optional<EnumColumn> enumColumnOptional = currentFieldData.getEnumColumnAnnotation();
        if (enumColumnOptional.isPresent()) {
            EnumColumn enumColumn = enumColumnOptional.get();
            switch (enumColumn.value()) {
                case STRING -> {
                    Enum<?> enumValue = (Enum<?>) arguments[i - 1];
                    statement.setObject(i, enumValue.toString());
                }
                case ORDINAL -> {
                    Enum<?> enumValue = (Enum<?>) arguments[i - 1];
                    statement.setObject(i, enumValue.ordinal());
                }
                case BY_ID -> {
                    EnumId enumValue = (EnumId) arguments[i - 1];
                    statement.setObject(i, enumValue.getId());
                }
            }
        } else {
            statement.setObject(i, arguments[i - 1]);
        }
    }

    private void parseQueryMethod(Class<?> returnClass, Class<?> entityClass, String methodName, ClassDataCache classDataCache, MethodMetadata methodMetadata) throws ClassCacheNotFoundException {
        PartParser partParser = new PartParser();
        partParser.parse(methodName);
        List<Part> parts = partParser.getParts();

        List<FieldDataCache> fieldDataCacheList = classDataCache.getFields();
        List<FieldData> fieldsToFetch;

        if (!returnClass.equals(entityClass)) {
            ProjectionClassData projectionClassData = projectionClassDataCache.get(returnClass, fieldDataCacheList);
            fieldsToFetch = new ArrayList<>(projectionClassData.getFields());
        } else {
            fieldsToFetch = getFieldsToFetch(fieldDataCacheList);
        }

        QueryBuilder queryBuilder = new QueryBuilder(parts, classDataCache.getTableName(), fieldDataCacheList, fieldsToFetch);
        CustomQueryMetadata query = queryBuilder.createQuery();
        query.setPullType(getPullType(methodMetadata));
        queryMetadataCache.addToCache(methodName, query);
    }

    private List<FieldData> getFieldsToFetch(List<FieldDataCache> fieldDataCacheList) throws ClassCacheNotFoundException {
        List<FieldData> result = new ArrayList<>();
        for (FieldDataCache fieldData : fieldDataCacheList) {
            if (fieldData.getEmbeddedAnnotation().isPresent()) {
                EmbeddableClassData embeddableClassData = bootstrap.getEmbeddableClassDataCache(fieldData.getType());
                result.addAll(getFieldsToFetch(embeddableClassData.getFields()));
            } else {
                result.add(fieldData);
            }
        }
        return result;
    }

    private static QueryPullType getPullType(MethodMetadata methodMetadata) {
        Type returnType = methodMetadata.getReturnTypeMetadata().getResolvedType();

        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                if (rawClass.equals(Optional.class)) {
                    return QueryPullType.SINGLE;
                } else if (rawClass.equals(List.class)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?>) {
                        return QueryPullType.MULTIPLE;
                    }
                }
            }
        } else if (returnType instanceof Class<?>) {
            return QueryPullType.NONE;
        }

        return null;
    }
}
