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
import com.altinntech.clicksave.core.query.preprocessor.QueryAnnotationPreprocessor;
import com.altinntech.clicksave.core.query.preprocessor.QueryMethodNamePreprocessor;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.EnumId;
import com.altinntech.clicksave.interfaces.QueryInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.altinntech.clicksave.core.ClicksaveInternalRepository.executePostLoadedMethods;

/**
 * The {@code QueryExecutor} class is responsible for executing assembled queries.
 * It processes queries and returns objects or lists of objects from the database.
 *
 * @author Fyodor Plotnikov, Anton Volkov
 */
public class QueryExecutor {

    private final ConnectionManager connectionManager;
    private final ClassDataCacheService classDataCacheService;
    private final BatchCollector batchCollector;
    private final SyncManager syncManager;
    private final ThreadPoolManager threadPoolManager;
    private final QueryMetadataCache queryMetadataCache = QueryMetadataCache.getInstance();
    private final ProjectionClassDataCache projectionClassDataCache = ProjectionClassDataCache.getInstance();
    private final QueryAnnotationPreprocessor annotationPreprocessor;
    private final QueryMethodNamePreprocessor methodNamePreprocessor;

    /**
     * Constructs a new QueryExecutor instance.
     */
    public QueryExecutor(ConnectionManager connectionManager, ClassDataCacheService classDataCacheService, BatchCollector batchCollector, SyncManager syncManager, ThreadPoolManager threadPoolManager) {
        this.connectionManager = connectionManager;
        this.classDataCacheService = classDataCacheService;
        this.batchCollector = batchCollector;
        this.syncManager = syncManager;
        this.threadPoolManager = threadPoolManager;
        this.annotationPreprocessor = new QueryAnnotationPreprocessor(queryMetadataCache);
        this.methodNamePreprocessor = new QueryMethodNamePreprocessor(queryMetadataCache, classDataCacheService);
    }

    /**
     * Processes the query and returns the result object.
     *
     * @param queryInfo Compiled query info from annotation/method metadata/supplied by application manually
     *
     * @return the result object
     *
     * @throws ClassCacheNotFoundException if class cache is not found
     * @throws SQLException                if an SQL exception occurs
     */
    public Object processQuery(QueryInfo queryInfo) throws ClassCacheNotFoundException, SQLException, IllegalAccessException, InvocationTargetException {
        ClassDataCache classDataCache = classDataCacheService.getClassDataCache(queryInfo.entityClass());
        threadPoolManager.waitForCompletion();
        syncManager.saveBatchRequest();
        batchCollector.saveAndFlush(classDataCache);

        List<Object> argumentsList = queryInfo.args();

        String queryId = queryInfo.queryId();

        if (!queryMetadataCache.containsKey(queryId)) {
            if (queryInfo.isParsedByMethodName()) {
                queryId = methodNamePreprocessor.preprocessQuery(queryInfo);
            } else {
                queryId = annotationPreprocessor.preprocessQuery(queryInfo);
            }
            assert Objects.equals(queryId, queryInfo.queryId());
        }

        CustomQueryMetadata query = (CustomQueryMetadata) queryMetadataCache.getFromCache(queryId);

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(query.getQueryBody())
        ) {
            int paramCount = countParameters(query.getQueryBody(), argumentsList);
            if (query.getIsQueryFromAnnotation()) {
                for (int i = 0; i < paramCount; i++) {
                    statement.setObject(i + 1, argumentsList.get(i));
                }
            } else {
                for (int i = 0; i < paramCount; i++) {
                    setStatementArgument(argumentsList, query, statement, i);
                }
            }

            switch (query.getPullType()) {
                case SINGLE -> {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Object entity;
                            if (!queryInfo.returnClass().equals(queryInfo.entityClass())) {
                                entity = CSUtils.createDtoEntityFromResultSet(queryInfo.returnClass(), resultSet);
                            } else {
                                entity = CSUtils.createEntityFromResultSet(queryInfo.entityClass(), resultSet, classDataCache, classDataCacheService);
                            }
                            connectionManager.releaseConnection(connection);
                            executePostLoadedMethods(entity, classDataCache);
                            return Optional.ofNullable(entity);
                        } else {
                            return Optional.empty();
                        }
                    }

                }
                case MULTIPLE -> {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        List<Object> entities = new ArrayList<>();
                        while (resultSet.next()) {
                            Object entity;
                            if (!queryInfo.returnClass().equals(queryInfo.entityClass())) {
                                entity = CSUtils.createDtoEntityFromResultSet(queryInfo.returnClass(), resultSet);
                            }
                            else {
                                entity = CSUtils.createEntityFromResultSet(queryInfo.entityClass(), resultSet, classDataCache, classDataCacheService);
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

    private static void setStatementArgument(List<Object> arguments, CustomQueryMetadata query, PreparedStatement statement, int i) throws SQLException {
        FieldDataCache currentFieldData = query.getFields().get(i);
        Optional<EnumColumn> enumColumnOptional = currentFieldData.getEnumColumnAnnotation();
        if (enumColumnOptional.isPresent()) {
            EnumColumn enumColumn = enumColumnOptional.get();
            switch (enumColumn.value()) {
                case STRING -> {
                    Enum<?> enumValue = (Enum<?>) arguments.get(i);
                    statement.setObject(i + 1, enumValue.toString());
                }
                case ORDINAL -> {
                    Enum<?> enumValue = (Enum<?>) arguments.get(i);
                    statement.setObject(i + 1, enumValue.ordinal());
                }
                case BY_ID -> {
                    EnumId enumValue = (EnumId) arguments.get(i);
                    statement.setObject(i + 1, enumValue.getId());
                }
            }
        } else {
            statement.setObject(i + 1, arguments.get(i));
        }
    }

    private static int countParameters(String query, List<Object> args) {
        int count = 0;
        int index = 0;
        while ((index = query.indexOf("?", index)) != -1) {
            count++;
            index += 1;
        }
        assert count == args.size();
        return count;
    }
}
