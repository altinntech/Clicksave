package com.altinntech.clicksave.core.utils.tb;

import com.altinntech.clicksave.annotations.Embedded;
import com.altinntech.clicksave.core.CSBootstrap;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.ColumnData;
import com.altinntech.clicksave.core.dto.EmbeddableClassData;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.enums.EngineType;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;

import java.util.List;
import java.util.Optional;

import static com.altinntech.clicksave.log.CSLogger.info;

public class TableBuilder {

    private final CSBootstrap bootstrap;

    public TableBuilder(CSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void generateTable(Class<?> clazz) throws ClassCacheNotFoundException {
        StringBuilder primaryKey = new StringBuilder();

        ClassDataCache classDataCache = bootstrap.getClassDataCache(clazz);
        EngineType engineType = classDataCache.getEngineType();

        String tableName = classDataCache.getTableName();
        List<FieldDataCache> fields = classDataCache.getFields();
        info("Creating table " + tableName);

        StringBuilder query = new StringBuilder("CREATE TABLE ");
        query.append(tableName).append(" (");

        prebuildTable(primaryKey, fields, query);

        switch (engineType) {
            case MergeTree ->
                    TableAdditionsResolver.buildForMergeTree(query, classDataCache, primaryKey);
            case VersionedCollapsingMergeTree ->
                    TableAdditionsResolver.buildForVersionedCollapsingMergeTree(query, classDataCache, primaryKey);
        }
        
        bootstrap.executeQuery(query.toString());
    }

    private void prebuildTable(StringBuilder primaryKey, List<FieldDataCache> fields, StringBuilder query) throws ClassCacheNotFoundException {
        for (FieldDataCache fieldData : fields) {
            String fieldName = fieldData.getFieldInTableName();
            Optional<Embedded> embeddedOptional = fieldData.getEmbeddedAnnotation();

            if (fieldData.getFieldType() == null && embeddedOptional.isEmpty()) {
                throw new FieldInitializationException("Not valid field: " + fieldData);
            }

            if (embeddedOptional.isPresent()) {
                EmbeddableClassData embeddableClassData = bootstrap.getEmbeddableClassDataCache(fieldData.getType());
                if (embeddableClassData != null) {
                    prebuildTable(primaryKey, embeddableClassData.getFields(), query);
                    continue;
                } else {
                    throw new FieldInitializationException("Embeddable class of field '" + fieldData.getFieldName() + "' not found");
                }
            } else {
                query.append(fieldName).append(" ");
                query.append(fieldData.getFieldType().getType());

                if (fieldData.isPk()) {
                    if (primaryKey.length() == 0) {
                        primaryKey.append(fieldName);
                    } else {
                        primaryKey.append(", ").append(fieldName);
                    }
                }
            }

            query.append(", ");
        }
    }

    public void updateTable(Class<?> clazz) throws FieldInitializationException, ClassCacheNotFoundException {
        ClassDataCache classDataCache = bootstrap.getClassDataCache(clazz);
        if (classDataCache.getSystemTableAnnotationOptional().isPresent()) {
            return;
        }

        String tableName = classDataCache.getTableName();
        info("Check for updates table " + tableName);
        List<ColumnData> tableFieldsFromDB = bootstrap.fetchTableColumns(tableName);
        List<FieldDataCache> fields = classDataCache.getFields();
        checkFields(tableName, fields, tableFieldsFromDB);
    }

    private void checkFields(String tableName, List<FieldDataCache> fieldDataCaches, List<ColumnData> fieldsFromDB) throws ClassCacheNotFoundException {
        for (FieldDataCache fieldData : fieldDataCaches) {
            String fieldName = fieldData.getFieldInTableName();

            // check for existing
            boolean exists = fieldsFromDB.stream()
                    .anyMatch(columnData -> columnData.getColumnName().equals(fieldName));
            if (!exists && fieldData.getEmbeddedAnnotation().isEmpty()) {
                addColumn(tableName, fieldData);
            } else if (fieldData.getEmbeddedAnnotation().isPresent()) {
                EmbeddableClassData embeddableClassData = bootstrap.getEmbeddableClassDataCache(fieldData.getType());
                if (embeddableClassData != null) {
                    checkFields(tableName, embeddableClassData.getFields(), fieldsFromDB);
                }
            }

            //check for types
            if (fieldData.getEmbeddedAnnotation().isEmpty()) {
                String fieldType = fieldData.getFieldType().getType();
                boolean concurrence = fieldsFromDB.stream()
                        .anyMatch(columnData -> columnData.getColumnName().equals(fieldName) &&
                                columnData.getColumnType().equals(fieldType));
                if (exists && !concurrence) {
                    modifyColumn(tableName, fieldData);
                }
            }
        }
    }

    private void addColumn(String tableName, FieldDataCache fieldData) {
        String fieldName = fieldData.getFieldInTableName();
        String dataType = fieldData.getFieldType().getType();
        String queryBuilder = "ALTER TABLE " + tableName + " ADD COLUMN" +
                " " + fieldName + " " + dataType;
        bootstrap.executeQuery(queryBuilder);
        info("Add column '" + fieldName + "' into table '" + tableName + "'");
    }

    private void modifyColumn(String tableName, FieldDataCache fieldData) {
        String fieldName = fieldData.getFieldInTableName();
        String dataType = fieldData.getFieldType().getType();
        String queryBuilder = "ALTER TABLE " + tableName + " MODIFY COLUMN" +
                " " + fieldName + " " + dataType;
        bootstrap.executeQuery(queryBuilder);
        info("Modify column '" + fieldName + "' into table '" + tableName + "'");
    }
}
