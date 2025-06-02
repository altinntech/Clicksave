package com.altinntech.clicksave.core.dto;

import java.util.List;

public interface ClassData {

    void setFields(List<FieldDataCache> fields);
    void setIdField(FieldDataCache idField);
    List<FieldDataCache> getFields();
}
