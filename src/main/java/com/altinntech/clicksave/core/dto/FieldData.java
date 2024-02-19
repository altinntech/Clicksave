package com.altinntech.clicksave.core.dto;

import java.lang.reflect.Field;

public interface FieldData {
    Field getField();
    String getFieldName();
    String getFieldInTableName();
}
