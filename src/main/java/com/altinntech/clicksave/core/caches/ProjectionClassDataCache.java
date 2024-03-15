package com.altinntech.clicksave.core.caches;

import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.dto.PreparedFieldsData;
import com.altinntech.clicksave.core.dto.ProjectionClassData;

import java.lang.reflect.Field;
import java.util.*;

import static com.altinntech.clicksave.core.CSUtils.getFieldsData;

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
        if (!cache.containsKey(clazz))
            put(clazz);
        return cache.get(clazz);
    }

    private void put(Class<?> clazz) {
        ProjectionClassData projectionClassData = new ProjectionClassData();
        PreparedFieldsData preparedFieldsData = getFieldsData(clazz);
        projectionClassData.setFields(preparedFieldsData.getFields());
        cache.put(clazz, projectionClassData);
    }
}
