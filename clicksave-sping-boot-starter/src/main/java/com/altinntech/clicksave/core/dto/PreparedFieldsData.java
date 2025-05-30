package com.altinntech.clicksave.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreparedFieldsData {

    List<FieldDataCache> fields;
    FieldDataCache idField;
    int idFieldsCount;
}
