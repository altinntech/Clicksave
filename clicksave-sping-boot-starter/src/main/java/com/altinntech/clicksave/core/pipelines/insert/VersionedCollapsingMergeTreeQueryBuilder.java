package com.altinntech.clicksave.core.pipelines.insert;

import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.enums.SystemField;

import java.util.List;

public class VersionedCollapsingMergeTreeQueryBuilder implements InsertQueryBuilder {
    @Override
    public String buildInsertQuery(StringBuilder insertQuery, StringBuilder valuesPlaceholder, ClassDataCache classDataCache, List<Object> fieldValues) {
        insertQuery.append(SystemField.Sign.getName()).append(", ").append(SystemField.Version.getName()).append(", ");
        valuesPlaceholder.append("?, ?, ");
        fieldValues.add(1);
        fieldValues.add(1);

        insertQuery.delete(insertQuery.length() - 2, insertQuery.length()).append(")");
        valuesPlaceholder.delete(valuesPlaceholder.length() - 2, valuesPlaceholder.length()).append(")");
        return insertQuery + valuesPlaceholder.toString();
    }
}
