package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.EmbeddableClassData;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClassDataCacheService implements Disposable {

    private final Map<Class<?>, ClassDataCache> classDataCacheMap = new HashMap<>();
    private final Map<Class<?>, EmbeddableClassData> embeddableClassDataCacheMap = new HashMap<>();

    public void putClassDataCache(Class<?> clazz, ClassDataCache classData) {
        classDataCacheMap.put(clazz, classData);
    }

    public void putEmbeddableClassDataCache(Class<?> clazz, EmbeddableClassData classData) {
        embeddableClassDataCacheMap.put(clazz, classData);
    }

    /**
     * Retrieves the class data cache for the specified class.
     *
     * @param clazz the class
     * @return the class data cache
     * @throws ClassCacheNotFoundException if class cache is not found
     */
    public ClassDataCache getClassDataCache(Class<?> clazz) throws ClassCacheNotFoundException {
        Optional<ClassDataCache> classDataCacheOptional = Optional.ofNullable(classDataCacheMap.get(clazz));
        if (classDataCacheOptional.isPresent()) {
            return classDataCacheOptional.get();
        }
        throw new ClassCacheNotFoundException();
    }

    /**
     * Retrieves the embeddable class data cache for the specified class.
     *
     * @param clazz the class
     * @return the embeddable class data cache
     * @throws ClassCacheNotFoundException if class cache is not found
     */
    public EmbeddableClassData getEmbeddableClassDataCache(Class<?> clazz) throws ClassCacheNotFoundException {
        Optional<EmbeddableClassData> classDataCacheOptional = Optional.ofNullable(embeddableClassDataCacheMap.get(clazz));
        if (classDataCacheOptional.isPresent()) {
            return classDataCacheOptional.get();
        }
        throw new ClassCacheNotFoundException();
    }

    @Override
    public void dispose() {
        classDataCacheMap.clear();
        embeddableClassDataCacheMap.clear();
    }
}
