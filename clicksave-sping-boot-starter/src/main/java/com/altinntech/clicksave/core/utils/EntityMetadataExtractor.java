package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.core.dto.ClassData;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
class EntityMetadataExtractor {

    public ClassData parseEntity(Class<?> entityClass, ClassData entityClassData) {
        Field[] fields = entityClass.getDeclaredFields();
        List<FieldDataCache> result = new ArrayList<>();
        for (Field field : fields) {
            FieldDataCache fieldData = new FieldDataCache();
        }

        return entityClassData;
    }
}
