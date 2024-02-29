package com.altinntech.clicksave.core.dto;

import java.lang.reflect.Field;
import java.util.List;

/**
 * The {@code FieldData} interface defines methods to access field data.
 * It provides functionality to retrieve the field, field name, and field name in the table.
 *
 * <p>This interface is intended for use with certain DTOs.</p>
 *
 * @author Fyodor Plotnikov
 */
public interface FieldData {
    /**
     * Retrieves the field.
     *
     * @return the field
     */
    Field getField();

    /**
     * Retrieves the field name.
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Retrieves the field name in the table.
     *
     * @return the field name in the table
     */
    String getFieldInTableName();
}

