package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.core.caches.ProjectionClassDataCache;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.dto.ProjectionClassData;
import com.altinntech.clicksave.core.dto.ProjectionFieldData;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import com.altinntech.clicksave.exceptions.EntityInitializationException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;
import com.altinntech.clicksave.interfaces.EnumId;
import com.altinntech.clicksave.log.CSLogger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static com.altinntech.clicksave.log.CSLogger.*;

/**
 * The {@code CSUtils} class provides various utility methods for common operations.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public class CSUtils {

    /**
     * Constructs a new CSUtils instance.
     * This class only provides static utility methods and cannot be instantiated.
     */
    private CSUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Builds a snake-case table name from the given class name.
     *
     * @param clazz the class
     * @return the snake-case table name
     */
    static String buildTableName(Class<?> clazz) {
        String className = clazz.getSimpleName();
        return toSnakeCase(className);
    }

    /**
     * Converts the input string to snake case.
     *
     * @param input the input string
     * @return the string converted to snake case
     */

    public static String toSnakeCase(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                if (i > 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }

    /**
     * Retrieves and initializes the data of fields for the given class and stores it in the provided ClassDataCache.
     *
     * @param clazz          the class
     * @param classDataCache the class data cache
     * @return the list of field data caches
     * @throws FieldInitializationException if there is an issue initializing the fields
     */
    static List<FieldDataCache> getFieldsData(Class<?> clazz, ClassDataCache classDataCache) throws FieldInitializationException {
        Field[] fields = clazz.getDeclaredFields();
        List<FieldDataCache> result = new ArrayList<>();
        // always must be one
        int idFieldsCount = 0;
        FieldDataCache idField = null;
        for (Field field : fields) {
            FieldDataCache fieldData = new FieldDataCache();
            boolean isPersistent = false;
            boolean isId = false;
            String fieldTableName = toSnakeCase(field.getName());
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Column column) {
                    isPersistent = true;
                    fieldData.setColumnAnnotation(column);
                    if (column.id()) {
                        if (column.value() != FieldType.UUID)
                            throw new FieldInitializationException("Id column must be a UUID column type");
                        idFieldsCount++;
                        isId = true;
                    }
                } else if (annotation instanceof EnumColumn enumerated) {
                    if (enumerated.value() == EnumType.BY_ID && !EnumId.class.isAssignableFrom(fieldType)) {
                        throw new FieldInitializationException("Enum " + fieldType.getSimpleName() + " must implements " + EnumId.class.getSimpleName() + " interface");
                    }
                    isPersistent = true;
                    fieldData.setEnumColumnAnnotation(enumerated);
                }
            }

            if (isPersistent) {
                fieldData.setField(field);
                fieldData.setFieldName(fieldName);
                fieldData.setFieldInTableName(fieldTableName);
                fieldData.setType(fieldType);
                result.add(fieldData);
                if (isId) {
                    idField = fieldData;
                }
            }
        }
        if (idFieldsCount != 1)
            throw new EntityInitializationException("Entity must have one id field");
        classDataCache.setFields(result);
        classDataCache.setIdField(idField);
        return result;
    }

    /**
     * Sets the value of the given field on the entity object.
     *
     * @param entity    the entity object
     * @param field     the field to set the value for
     * @param value     the value to set
     * @param fieldData the field data cache
     * @throws IllegalAccessException if there is an issue accessing the field
     */
    static void setFieldValue(Object entity, Field field, Object value, FieldDataCache fieldData) throws IllegalAccessException {
        Class<?> fieldType = fieldData.getType();
        field.setAccessible(true);

        if (isEnumAndString(fieldType, value)) {
            setEnumFieldValue(entity, field, (String) value, fieldData);
        } else if (isEnumIdAndLong(fieldType, value, fieldData)) {
            setEnumIdFieldValue(entity, field, (Long) value, fieldData);
        } else if (isEnumAndInteger(fieldType, value)) {
            setEnumOrdinalFieldValue(entity, field, (Integer) value, fieldData);
        } else if (isValidFieldValue(fieldType, value)) {
            field.set(entity, value);
        }
    }

    private static boolean isEnumAndString(Class<?> fieldType, Object value) {
        return fieldType.isEnum() && value instanceof String;
    }

    private static void setEnumFieldValue(Object entity, Field field, String enumValueName, FieldDataCache fieldData) {
        Enum<?> enumValue = Enum.valueOf((Class<Enum>) fieldData.getType(), enumValueName);
        setField(entity, field, enumValue);
    }

    private static boolean isEnumIdAndLong(Class<?> fieldType, Object value, FieldDataCache fieldData) {
        return EnumId.class.isAssignableFrom(fieldType) && value instanceof Long &&
                fieldData.getEnumColumnAnnotation().get().value() == EnumType.BY_ID;
    }

    private static void setEnumIdFieldValue(Object entity, Field field, Long id, FieldDataCache fieldData) {
        Class<? extends EnumId> enumClass = (Class<? extends EnumId>) fieldData.getType();
        EnumId enumValue = getEnumById(enumClass, id);
        setField(entity, field, enumValue);
    }

    private static boolean isEnumAndInteger(Class<?> fieldType, Object value) {
        return fieldType.isEnum() && value instanceof Integer;
    }

    private static void setEnumOrdinalFieldValue(Object entity, Field field, Integer enumOrdinal, FieldDataCache fieldData) {
        Enum<?>[] enumConstants = (Enum<?>[]) fieldData.getType().getEnumConstants();
        if (isValidEnumOrdinal(enumOrdinal, enumConstants.length)) {
            setField(entity, field, enumConstants[enumOrdinal]);
        } else {
            throw new IllegalArgumentException("Invalid ordinal value for enum: " + enumOrdinal);
        }
    }

    private static boolean isValidEnumOrdinal(Integer enumOrdinal, int enumConstantsLength) {
        return enumOrdinal >= 0 && enumOrdinal < enumConstantsLength;
    }

    private static boolean isValidFieldValue(Class<?> fieldType, Object value) {
        return value != null && fieldType.isAssignableFrom(value.getClass());
    }

    private static void setField(Object entity, Field field, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            error(e.getMessage());
        }
    }

    /**
     * Retrieves an enum constant by its ID.
     *
     * @param <T>       the type parameter
     * @param enumClass the enum class
     * @param id        the ID of the enum constant
     * @return the enum constant with the specified ID
     * @throws IllegalArgumentException if no enum constant with the specified ID is found
     */
    static <T extends EnumId> T getEnumById(Class<T> enumClass, Long id) throws IllegalArgumentException {
        T[] enumConstants = enumClass.getEnumConstants();
        for (T enumValue : enumConstants) {
            if (enumValue.getId().equals(id)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant with id: " + id);
    }

    /**
     * Finds the FieldDataCache object by field name in the ClassDataCache.
     *
     * @param fieldName      the name of the field to find
     * @param classDataCache the ClassDataCache containing field data
     * @return the FieldDataCache object corresponding to the field name, or null if not found
     */
    private static FieldDataCache findFieldDataCache(String fieldName, ClassDataCache classDataCache) {
        List<FieldDataCache> fetchedEntityFieldsData = classDataCache.getFields();
        return fetchedEntityFieldsData.stream()
                .filter(fieldDataCache -> fieldDataCache.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates a DTO entity object from a ResultSet.
     *
     * @param <T>            the type parameter
     * @param returnType     the return type of the DTO entity
     * @param resultSet      the ResultSet containing data
     * @param classDataCache the ClassDataCache containing field metadata
     * @return the DTO entity object created from the ResultSet
     * @throws SQLException the SQL exception
     */
    public static <T> T createDtoEntityFromResultSet(Class<T> returnType, ResultSet resultSet, ClassDataCache classDataCache) throws SQLException {
        T entity = null;
        ProjectionClassDataCache projectionClassDataCache = ProjectionClassDataCache.getInstance();
        ProjectionClassData projectionClassData = projectionClassDataCache.get(returnType, classDataCache.getFields());

        try {
            entity = returnType.getDeclaredConstructor().newInstance();
            List<ProjectionFieldData> fieldDataList = projectionClassData.getFields();
            for (ProjectionFieldData fieldData : fieldDataList) {
                Field field = fieldData.getField();
                field.setAccessible(true);
                Optional<Reference> referenceAnnotationOptional = fieldData.getReferenceAnnotationOptional();

                if (referenceAnnotationOptional.isPresent()) {
                    String fieldNameInEntity = referenceAnnotationOptional.get().value();
                    setValueFromResultSet(resultSet, classDataCache, entity, field, fieldNameInEntity);
                } else {
                    String fieldNameInEntity = fieldData.getFieldName();
                    setValueFromResultSet(resultSet, classDataCache, entity, field, fieldNameInEntity);
                }

            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return entity;
    }

    private static <T> void setValueFromResultSet(ResultSet resultSet, ClassDataCache classDataCache, T entity, Field field, String fieldNameInEntity) throws SQLException, IllegalAccessException {
        FieldDataCache fieldDataCache = findFieldDataCache(fieldNameInEntity, classDataCache);
        if (fieldDataCache != null) {
            String columnName = fieldDataCache.getFieldInTableName();
            boolean columnFound = isColumnFound(resultSet, columnName);
            if(columnFound) {
                Object value = resultSet.getObject(columnName);
                setFieldValue(entity, field, value, fieldDataCache);
            } else {
                warn("Column '" + columnName + "' not found in resultSet");
            }
        }
    }

    /**
     * Creates an entity object from a ResultSet.
     *
     * @param <T>            the type parameter
     * @param entityClass    the entity class
     * @param resultSet      the ResultSet containing data
     * @param classDataCache the ClassDataCache containing field metadata
     * @return the entity object created from the ResultSet
     * @throws SQLException             the SQL exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    public static <T> T createEntityFromResultSet(Class<T> entityClass, ResultSet resultSet, ClassDataCache classDataCache) throws SQLException, IllegalArgumentException {
        T entity = null;
        try {
            entity = entityClass.getDeclaredConstructor().newInstance();
            List<FieldDataCache> fields = classDataCache.getFields();
            for (FieldDataCache fieldDataCache : fields) {
                Field field = fieldDataCache.getField();
                String columnName = fieldDataCache.getFieldInTableName();
                boolean columnFound = isColumnFound(resultSet, columnName);

                if(columnFound) {
                    Object value = resultSet.getObject(columnName);
                    setFieldValue(entity, field, value, fieldDataCache);
                } else {
                    warn("Column '" + columnName + "' not found in resultSet");
                }
            }
        } catch (InstantiationException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            error(e.getMessage());
        }
        return entity;
    }

    private static boolean isColumnFound(ResultSet resultSet, String columnName) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        boolean columnFound = IntStream.range(1, columnCount + 1)
                .mapToObj(i -> {
                    try {
                        return metaData.getColumnName(i);
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to get column name", e);
                    }
                })
                .anyMatch(columnName::equals);
        return columnFound;
    }

    /**
     * Gets a random enum constant from the specified enum class.
     *
     * @param <T>       the type parameter
     * @param enumClass the enum class
     * @return a random enum constant from the specified enum class
     */
    public static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        Random random = new Random();
        int enumLength = enumClass.getEnumConstants().length;
        int randomIndex = random.nextInt(enumLength);
        return enumClass.getEnumConstants()[randomIndex];
    }

    /**
     * Generates a random string of the specified length.
     *
     * @param length the length of the random string
     * @return the randomly generated string
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    /**
     * Generates a random integer within the specified range.
     *
     * @param min the minimum value of the random number (inclusive)
     * @param max the maximum value of the random number (inclusive)
     * @return the randomly generated integer
     */
    public static int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
