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
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.altinntech.clicksave.log.CSLogger.error;

public class CSUtils {

    static String buildTableName(Class<?> clazz) {
        String className = clazz.getSimpleName();
        return toSnakeCase(className);
    }

    @NotNull
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

    // todo: need to refactor
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

    static <T extends EnumId> T getEnumById(Class<T> enumClass, Long id) throws IllegalArgumentException {
        T[] enumConstants = enumClass.getEnumConstants();
        for (T enumValue : enumConstants) {
            if (enumValue.getId().equals(id)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant with id: " + id);
    }

    private static FieldDataCache findFieldDataCache(String fieldName, ClassDataCache classDataCache) {
        List<FieldDataCache> fetchedEntityFieldsData = classDataCache.getFields();
        return fetchedEntityFieldsData.stream()
                .filter(fieldDataCache -> fieldDataCache.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

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
                    FieldDataCache fieldDataCache = findFieldDataCache(fieldNameInEntity, classDataCache);
                    if (fieldDataCache != null) {
                        Object value = resultSet.getObject(fieldDataCache.getFieldInTableName());
                        setFieldValue(entity, field, value, fieldDataCache);
                    }
                } else {
                    String fieldNameInEntity = fieldData.getFieldName();
                    FieldDataCache fieldDataCache = findFieldDataCache(fieldNameInEntity, classDataCache);
                    if (fieldDataCache != null) {
                        Object value = resultSet.getObject(fieldDataCache.getFieldInTableName());
                        setFieldValue(entity, field, value, fieldDataCache);
                    }
                }

            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return entity;
    }

    public static <T> T createEntityFromResultSet(Class<T> entityClass, ResultSet resultSet, ClassDataCache classDataCache) throws SQLException, IllegalArgumentException {
        T entity = null;
        try {
            entity = entityClass.getDeclaredConstructor().newInstance();
            List<FieldDataCache> fields = classDataCache.getFields();
            for (FieldDataCache fieldDataCache : fields) {
                Field field = fieldDataCache.getField();
                String columnName = fieldDataCache.getFieldInTableName();
                Object value = resultSet.getObject(columnName);
                setFieldValue(entity, field, value, fieldDataCache);
            }
        } catch (InstantiationException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            error(e.getMessage());
        }
        return entity;
    }

    public static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        Random random = new Random();
        int enumLength = enumClass.getEnumConstants().length;
        int randomIndex = random.nextInt(enumLength);
        return enumClass.getEnumConstants()[randomIndex];
    }

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

    public static int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
