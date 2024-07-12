package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.enums.FieldType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * The {@code FieldDataCache} class is a DTO used to store information about entity fields.
 * It holds the field, field name, field name in the table, field type, and annotations such as Column and EnumColumn.
 *
 * <p>This class implements the {@code FieldData} interface.</p>
 *
 * <p>This DTO is used to cache information about entity fields.</p>
 *
 * <p>{@code columnAnnotation} represents the {@code Column} annotation, and {@code enumColumnAnnotation}
 * represents the {@code EnumColumn} annotation associated with the field.</p>
 *
 * <p>The {@code fieldInTableName} field represents the field name in the table format.</p>
 *
 * <p>This class provides methods to retrieve optional annotations for the field.</p>
 *
 * <p>The constructor initializes the FieldDataCache instance with field-related information.</p>
 *
 * @author Fyodor Plotnikov
 */
@Data
@NoArgsConstructor
public class FieldDataCache implements FieldData, Serializable {

    @Serial
    private transient static final long serialVersionUID = 49438156L;

    private Field field;
    private String fieldName;
    private String fieldInTableName;
    private Class<?> type;
    private FieldType fieldType;
    private boolean isId;
    private boolean isPk;
    private boolean isEnum;
    private boolean isLob;
    private boolean isEmbedded;
    private boolean isNullable;
    private Column columnAnnotation;
    private EnumColumn enumColumnAnnotation;
    private Embedded embeddedAnnotation;
    private Lob lobAnnotation;
    private Reference referenceAnnotation;

    /**
     * Retrieves the optional Column annotation associated with the field.
     *
     * @return the optional Column annotation
     */
    public Optional<Column> getColumnAnnotation() {
        return Optional.ofNullable(columnAnnotation);
    }

    /**
     * Retrieves the optional EnumColumn annotation associated with the field.
     *
     * @return the optional EnumColumn annotation
     */
    public Optional<EnumColumn> getEnumColumnAnnotation() {
        return Optional.ofNullable(enumColumnAnnotation);
    }

    /**
     * Retrieves the optional Embedded annotation associated with the field.
     *
     * @return the optional Embedded annotation
     */
    public Optional<Embedded> getEmbeddedAnnotation() {
        return Optional.ofNullable(embeddedAnnotation);
    }

    /**
     * Retrieves the optional Lob annotation associated with the field.
     *
     * @return the optional Lob annotation
     */
    public Optional<Lob> getLobAnnotation() {
        return Optional.ofNullable(lobAnnotation);
    }

    /**
     * Retrieves the optional Reference annotation associated with the field.
     *
     * @return the optional Reference annotation
     */
    public Optional<Reference> getReferenceAnnotationOptional() {
        return Optional.ofNullable(referenceAnnotation);
    }

    public String getFieldInTableName() {
        if (referenceAnnotation != null) {
            return referenceAnnotation.value();
        } else {
            return fieldInTableName;
        }
    }

    /**
     * Constructs a new FieldDataCache instance with the provided field.
     *
     * @param field the field
     */
    public FieldDataCache(Field field) {
        this.field = field;
        this.fieldName = field.getName();
        this.type = field.getType();
        this.columnAnnotation = field.getAnnotation(Column.class);
        this.enumColumnAnnotation = field.getAnnotation(EnumColumn.class);
        this.embeddedAnnotation = field.getAnnotation(Embedded.class);
        this.lobAnnotation = field.getAnnotation(Lob.class);
        this.fieldInTableName = CSUtils.toSnakeCase(fieldName);
    }
}
