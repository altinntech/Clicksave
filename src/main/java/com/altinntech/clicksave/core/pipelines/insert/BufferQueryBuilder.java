package com.altinntech.clicksave.core.pipelines.insert;

import com.altinntech.clicksave.core.dto.ClassDataCache;

import java.util.List;

public class BufferQueryBuilder implements InsertQueryBuilder {
    @Override
    public String buildInsertQuery(StringBuilder insertQuery, StringBuilder valuesPlaceholder, ClassDataCache classDataCache, List<Object> fieldValues) {
        insertQuery.delete(insertQuery.length() - 2, insertQuery.length()).append(")");
        valuesPlaceholder.delete(valuesPlaceholder.length() - 2, valuesPlaceholder.length()).append(")");
        return insertQuery + valuesPlaceholder.toString();
    }
}
