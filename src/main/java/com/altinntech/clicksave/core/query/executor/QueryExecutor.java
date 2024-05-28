package com.altinntech.clicksave.core.query.executor;

import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.annotations.Query;
import com.altinntech.clicksave.annotations.SettableQuery;
import com.altinntech.clicksave.core.*;
import com.altinntech.clicksave.core.caches.ProjectionClassDataCache;
import com.altinntech.clicksave.core.caches.QueryMetadataCache;
import com.altinntech.clicksave.core.dto.*;
import com.altinntech.clicksave.core.query.builder.QueryBuilder;
import com.altinntech.clicksave.core.query.builder.QueryPullType;
import com.altinntech.clicksave.core.query.parser.Part;
import com.altinntech.clicksave.core.query.parser.PartParser;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.EnumId;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

import static com.altinntech.clicksave.core.CHRepository.executePostLoadedMethods;

/**
 * The {@code QueryExecutor} class is responsible for executing assembled queries.
 * It processes queries and returns objects or lists of objects from the database.
 *
 * @author Fyodor Plotnikov
 */
public class QueryExecutor {

    private final ConnectionManager connectionManager;
    private final ClassDataCacheService classDataCacheService;
    private final BatchCollector batchCollector;
    private final QueryMetadataCache queryMetadataCache = QueryMetadataCache.getInstance();
    private final ProjectionClassDataCache projectionClassDataCache = ProjectionClassDataCache.getInstance();

    /**
     * Constructs a new QueryExecutor instance.
     */
    public QueryExecutor(ConnectionManager connectionManager, ClassDataCacheService classDataCacheService, BatchCollector batchCollector) {
        this.connectionManager = connectionManager;
        this.classDataCacheService = classDataCacheService;
        this.batchCollector = batchCollector;
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
    public Object processQuery(Class<?> returnClass, Class<?> entityClass, Object[] arguments, MethodMetadata methodMetadata) throws ClassCacheNotFoundException, SQLException, IllegalAccessException, InvocationTargetException {
        ClassDataCache classDataCache = classDataCacheService.getClassDataCache(entityClass);
        batchCollector.saveAndFlush(classDataCache);

        List<Object> argumentsList = new ArrayList<>(Arrays.asList(arguments));

        String methodName = preprocessQueryObject(returnClass, entityClass, argumentsList, methodMetadata, classDataCache);

        CustomQueryMetadata query = (CustomQueryMetadata) queryMetadataCache.getFromCache(methodName);
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query.getQueryBody())) {
            int paramCount = countParameters(query.getQueryBody());
            if (query.getIsQueryFromAnnotation()) {
                for (int i = 1; i < argumentsList.size() + 1; i++) {
                    if (i <= paramCount) {
                        statement.setObject(i, argumentsList.get(i - 1));
                    }
                }
            } else {
                for (int i = 1; i < argumentsList.size() + 1; i++) {
                    if (i <= paramCount) {
                        setStatementArgument(argumentsList, query, statement, i);
                    }
                }
            }

            switch (query.getPullType()) {
                case SINGLE -> {

                    try(ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Object entity;
                            if (!returnClass.equals(entityClass))
                                entity = CSUtils.createDtoEntityFromResultSet(returnClass, resultSet);
                            else
                                entity = CSUtils.createEntityFromResultSet(entityClass, resultSet, classDataCache, classDataCacheService);
                            connectionManager.releaseConnection(connection);
                            executePostLoadedMethods(entity, classDataCache);
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
                            if (!returnClass.equals(entityClass)) {
                                entity = CSUtils.createDtoEntityFromResultSet(returnClass, resultSet);
                            }
                            else {
                                entity = CSUtils.createEntityFromResultSet(entityClass, resultSet, classDataCache, classDataCacheService);
                                executePostLoadedMethods(entity, classDataCache);
                            }
                            entities.add(entity);
                        }
                        connectionManager.releaseConnection(connection);
                        return entities;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //todo: refactor
    private String preprocessQueryObject(Class<?> returnClass, Class<?> entityClass, List<Object> arguments, MethodMetadata methodMetadata, ClassDataCache classDataCache) throws ClassCacheNotFoundException {
        String methodName = methodMetadata.getSourceMethod().getName();
        Annotation[] annotations = methodMetadata.getSourceMethod().getAnnotations();
        String queryBody = "";
        Query queryAnnotation = null;
        SettableQuery settableQuery = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Query.class) {
                queryAnnotation = (Query) annotation;
                break;
            } else if (annotation.annotationType() == SettableQuery.class) {
                settableQuery = (SettableQuery) annotation;
                break;
            }
        }

        if (settableQuery != null) {
            arguments.remove(0);
            methodName = String.valueOf((methodName + (String) arguments.get(0)).hashCode());
            queryBody = (String) arguments.get(0);
            arguments.remove(0);

            Object[] array = (Object[]) arguments.get(0);
            arguments.remove(0);
            arguments.addAll(Arrays.asList(array));

            arguments.removeIf(Objects::isNull);
        }
        if (!queryMetadataCache.containsKey(methodName)) {
            if (queryAnnotation != null) {
                CustomQueryMetadata customQueryMetadata = new CustomQueryMetadata();
                customQueryMetadata.setQueryBody(queryAnnotation.value());
                customQueryMetadata.setPullType(getPullType(methodMetadata));
                customQueryMetadata.setIsQueryFromAnnotation(true);
                queryMetadataCache.addToCache(methodName, customQueryMetadata);
            } else if (settableQuery != null) {
                CustomQueryMetadata customQueryMetadata = new CustomQueryMetadata();
                customQueryMetadata.setQueryBody(queryBody);
                customQueryMetadata.setPullType(getPullType(methodMetadata));
                customQueryMetadata.setIsQueryFromAnnotation(true);
                queryMetadataCache.addToCache(methodName, customQueryMetadata);
            } else {
                parseQueryMethod(returnClass, entityClass, methodName, classDataCache, methodMetadata);
            }
        }
        return methodName;
    }

    private static void setStatementArgument(List<Object> arguments, CustomQueryMetadata query, PreparedStatement statement, int i) throws SQLException {
        FieldDataCache currentFieldData = query.getFields().get(i - 1);
        Optional<EnumColumn> enumColumnOptional = currentFieldData.getEnumColumnAnnotation();
        if (enumColumnOptional.isPresent()) {
            EnumColumn enumColumn = enumColumnOptional.get();
            switch (enumColumn.value()) {
                case STRING -> {
                    Enum<?> enumValue = (Enum<?>) arguments.get(i - 1);
                    statement.setObject(i, enumValue.toString());
                }
                case ORDINAL -> {
                    Enum<?> enumValue = (Enum<?>) arguments.get(i - 1);
                    statement.setObject(i, enumValue.ordinal());
                }
                case BY_ID -> {
                    EnumId enumValue = (EnumId) arguments.get(i - 1);
                    statement.setObject(i, enumValue.getId());
                }
            }
        } else {
            statement.setObject(i, arguments.get(i - 1));
        }
    }

    private void parseQueryMethod(Class<?> returnClass, Class<?> entityClass, String methodName, ClassDataCache classDataCache, MethodMetadata methodMetadata) throws ClassCacheNotFoundException {
        PartParser partParser = new PartParser();
        partParser.parse(methodName);
        List<Part> parts = partParser.getParts();

        List<FieldDataCache> fieldDataCacheList = classDataCache.getFields();
        List<FieldData> fieldsToFetch;

        if (!returnClass.equals(entityClass)) {
            ProjectionClassData projectionClassData = projectionClassDataCache.get(returnClass);
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
                EmbeddableClassData embeddableClassData = classDataCacheService.getEmbeddableClassDataCache(fieldData.getType());
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

    private static int countParameters(String query) {
        int count = 0;
        int index = 0;

        while ((index = query.indexOf("?", index)) != -1) {
            count++;
            index += 1;
        }

        return count;
    }
}
