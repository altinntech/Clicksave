package com.altinntech.clicksave.core.caches;

import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.dto.ProjectionClassData;
import com.altinntech.clicksave.core.dto.ProjectionFieldData;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code ProjectionClassDataCache} class is a cache for storing metadata about projection classes.
 * It stores information about fields and their annotations for projection classes.
 *
 * @author Fyodor Plotnikov
 */
public class ProjectionClassDataCache {

    private static ProjectionClassDataCache projectionClassDataCache;
    private final Map<Class<?>, ProjectionClassData> cache = new HashMap<>();

    /**
     * Gets instance of the ProjectionClassDataCache.
     *
     * @return the instance of the ProjectionClassDataCache
     */
    public static ProjectionClassDataCache getInstance() {
        if (projectionClassDataCache == null) {
            projectionClassDataCache = new ProjectionClassDataCache();
        }
        return projectionClassDataCache;
    }

    private ProjectionClassDataCache() {
    }

    /**
     * Retrieves the ProjectionClassData for a given class.
     *
     * @param clazz the class for which to retrieve ProjectionClassData
     * @return the ProjectionClassData for the specified class
     */
    public ProjectionClassData get(Class<?> clazz) {
        return cache.get(clazz);
    }

    /**
     * Retrieves the ProjectionClassData for a given class.
     * If the class is not cached, it populates the cache with the provided originalEntityFieldDataList.
     *
     * @param clazz                       the class for which to retrieve ProjectionClassData
     * @param originalEntityFieldDataList the list of FieldDataCache representing fields in the original entity
     * @return the ProjectionClassData for the specified class
     */
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
