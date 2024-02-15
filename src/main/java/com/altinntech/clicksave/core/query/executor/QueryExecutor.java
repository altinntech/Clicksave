package com.altinntech.clicksave.core.query.executor;

import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.core.BatchCollector;
import com.altinntech.clicksave.core.CSBootstrap;
import com.altinntech.clicksave.core.caches.QueryMetadataCache;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.dto.CustomQueryMetadata;
import com.altinntech.clicksave.core.query.builder.QueryBuilder;
import com.altinntech.clicksave.core.query.parser.Part;
import com.altinntech.clicksave.core.query.parser.PartParser;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.EnumId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thepavel.icomponent.metadata.MethodMetadata;

import java.sql.*;
import java.util.*;

import static com.altinntech.clicksave.core.CSUtils.createEntityFromResultSet;

@Component
public class QueryExecutor {

    private final CSBootstrap bootstrap;
    private final BatchCollector batchCollector;
    private final QueryMetadataCache queryMetadataCache = QueryMetadataCache.getInstance();

    @Autowired
    public QueryExecutor(CSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.batchCollector = bootstrap.getBatchCollector();
    }

    public Object processQuery(Class<?> entityClass, Object[] arguments, MethodMetadata methodMetadata) throws ClassCacheNotFoundException, SQLException {
        String methodName = methodMetadata.getSourceMethod().getName();
        ClassDataCache classDataCache = bootstrap.getClassDataCache(entityClass);
        batchCollector.saveAndFlush(classDataCache);

        if (!queryMetadataCache.containsKey(methodName)) {
            PartParser partParser = new PartParser();
            partParser.parse(methodName);
            List<Part> parts = partParser.getParts();

            QueryBuilder queryBuilder = new QueryBuilder(parts, classDataCache.getTableName(), classDataCache.getFields());
            CustomQueryMetadata query = queryBuilder.createQuery();
            queryMetadataCache.addToCache(methodName, query);
        }

        CustomQueryMetadata query = (CustomQueryMetadata) queryMetadataCache.getFromCache(methodName);
        try(Connection connection = bootstrap.getConnection();
            PreparedStatement statement = connection.prepareStatement(query.getQueryBody())) {
            for (int i = 1; i < arguments.length + 1; i++) {
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

            switch (query.getPullType()) {
                case SINGLE -> {

                    try(ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Object entity = createEntityFromResultSet(entityClass, resultSet, classDataCache);
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
                            Object entity = createEntityFromResultSet(entityClass, resultSet, classDataCache);
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
}
