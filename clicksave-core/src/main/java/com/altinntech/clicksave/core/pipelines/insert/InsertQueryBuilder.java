package com.altinntech.clicksave.core.pipelines.insert;

import com.altinntech.clicksave.core.dto.ClassDataCache;

import java.util.List;

public interface InsertQueryBuilder {
    String buildInsertQuery(StringBuilder insertQuery, StringBuilder valuesPlaceholder, ClassDataCache classDataCache, List<Object> fieldValues);
}
