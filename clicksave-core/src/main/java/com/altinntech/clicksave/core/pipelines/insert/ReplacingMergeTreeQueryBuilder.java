package com.altinntech.clicksave.core.pipelines.insert;

import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.enums.SystemField;

import java.time.LocalDateTime;
import java.util.List;

public class ReplacingMergeTreeQueryBuilder implements InsertQueryBuilder {
    @Override
    public String buildInsertQuery(StringBuilder insertQuery, StringBuilder valuesPlaceholder, ClassDataCache classDataCache, List<Object> fieldValues) {
        insertQuery.append(SystemField.Timestamp.getName()).append(", ");
        valuesPlaceholder.append("?, ");
        fieldValues.add(LocalDateTime.now());

        insertQuery.delete(insertQuery.length() - 2, insertQuery.length()).append(")");
        valuesPlaceholder.delete(valuesPlaceholder.length() - 2, valuesPlaceholder.length()).append(")");
        return insertQuery + valuesPlaceholder.toString();
    }
}
