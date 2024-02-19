package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.core.CSUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Optional;

@Data
@NoArgsConstructor
public class FieldDataCache implements FieldData {

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

    public FieldDataCache(Field field) {
        this.field = field;
        this.fieldName = field.getName();
        this.type = field.getType();
        this.columnAnnotation = field.getAnnotation(Column.class);
        this.enumColumnAnnotation = field.getAnnotation(EnumColumn.class);
        this.fieldInTableName = CSUtils.toSnakeCase(fieldName);
    }
}
