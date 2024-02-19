package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Reference;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * The {@code ProjectionFieldData} class is a DTO used to store information about fields in projection classes.
 * It holds the field, field name, field name in the table, and Reference annotation.
 *
 * <p>This class implements the {@code FieldData} interface.</p>
 *
 * <p>{@code referenceAnnotation} represents the {@code Reference} annotation associated with the field.</p>
 *
 * <p>The {@code fieldInTableName} field represents the field name in the table format.</p>
 *
 * <p>This class provides a method to retrieve the optional Reference annotation for the field.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
@Data
@NoArgsConstructor
public class ProjectionFieldData implements FieldData {

    /**
     * The field.
     */
    Field field;

    /**
     * The field name.
     */
    String fieldName;

    /**
     * The field name in the table.
     */
    private String fieldInTableName;

    /**
     * The Reference annotation.
     */
    Reference referenceAnnotation;

    /**
     * Retrieves the optional Reference annotation associated with the field.
     *
     * @return the optional Reference annotation
     */
    public Optional<Reference> getReferenceAnnotationOptional() {
        return Optional.ofNullable(referenceAnnotation);
    }
}

