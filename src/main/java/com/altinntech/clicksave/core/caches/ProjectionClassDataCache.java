package com.altinntech.clicksave.core.caches;

import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.dto.ProjectionClassData;
import com.altinntech.clicksave.core.dto.ProjectionFieldData;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectionClassDataCache {

    private static ProjectionClassDataCache projectionClassDataCache;
    private final Map<Class<?>, ProjectionClassData> cache = new HashMap<>();

    public static ProjectionClassDataCache getInstance() {
        if (projectionClassDataCache == null) {
            projectionClassDataCache = new ProjectionClassDataCache();
        }
        return projectionClassDataCache;
    }

    private ProjectionClassDataCache() {
    }

    public ProjectionClassData get(Class<?> clazz) {
        return cache.get(clazz);
    }

    public ProjectionClassData get(Class<?> clazz, List<FieldDataCache> originalEntityFieldDataList) {
        if (!cache.containsKey(clazz))
            put(clazz, originalEntityFieldDataList);
        return cache.get(clazz);
    }

    private void put(Class<?> clazz, List<FieldDataCache> originalEntityFieldDataList) {
        ProjectionClassData projectionClassData = new ProjectionClassData();
        List<ProjectionFieldData> projectionFieldDataList = new ArrayList<>();

        Set<String> originalFieldNames = originalEntityFieldDataList.stream()
                .map(FieldDataCache::getFieldInTableName)
                .collect(Collectors.toSet());

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ProjectionFieldData projectionFieldData = new ProjectionFieldData();
            projectionFieldData.setField(field);
            projectionFieldData.setFieldName(field.getName());
            projectionFieldData.setReferenceAnnotation(field.getAnnotation(Reference.class));
            if (projectionFieldData.getReferenceAnnotationOptional().isPresent()) {
                projectionFieldData.setFieldInTableName(CSUtils.toSnakeCase(projectionFieldData.getReferenceAnnotationOptional().get().value()));
            } else {
                projectionFieldData.setFieldInTableName(CSUtils.toSnakeCase(projectionFieldData.getFieldName()));
            }

            if (originalFieldNames.contains(projectionFieldData.getFieldInTableName()))
                projectionFieldDataList.add(projectionFieldData);
        }

        projectionClassData.setFields(projectionFieldDataList);
        cache.put(clazz, projectionClassData);
    }
}
