package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Optional;

@Data
@NoArgsConstructor
public class FieldDataCache {

    private Field field;
    private String fieldName;
    private String fieldInTableName;
    private Class<?> type;
    private Column columnAnnotation;
    private EnumColumn enumColumnAnnotation;

    public Optional<Column> getColumnAnnotation() {
        return Optional.ofNullable(columnAnnotation);
    }

    public Optional<EnumColumn> getEnumColumnAnnotation() {
        return Optional.ofNullable(enumColumnAnnotation);
    }
}
