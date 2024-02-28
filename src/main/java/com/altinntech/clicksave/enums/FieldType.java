package com.altinntech.clicksave.enums;

/**
 * The {@code FieldType} enum represents different types of fields.
 * It includes various types such as NONE, INT, LONG, FLOAT, DOUBLE, STRING, UINT16, BIG_DECIMAL, and UUID.
 *
 * <p>Each enum constant includes a corresponding type name.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public enum FieldType {

    /**
     * Represents a field with no specific type.
     */
    NONE("Nothing"),

    /**
     * Represents an integer field type.
     */
    INT("Int32"),

    /**
     * Represents a long field type.
     */
    LONG("Int64"),

    /**
     * Represents a float field type.
     */
    FLOAT("Float32"),

    /**
     * Represents a double field type.
     */
    DOUBLE("Float64"),

    /**
     * Represents a string field type.
     */
    STRING("String"),

    /**
     * Represents a 16-bit unsigned integer field type.
     */
    UINT16("UInt16"),

    /**
     * Represents a big decimal field type.
     */
    BIG_DECIMAL("Decimal(20, 9)"),

    /**
     * Represents a UUID field type.
     */
    UUID("UUID");

    private final String type;

    /**
     * Constructs a FieldType enum constant with the given type name.
     *
     * @param type the type name
     */
    FieldType(String type) {
        this.type = type;
    }

    /**
     * Gets the type name associated with the FieldType.
     *
     * @return the type name
     */
    public String getType() {
        return type;
    }
}
