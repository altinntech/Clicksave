package com.altinntech.clicksave.core.query.builder;

import com.altinntech.clicksave.core.query.parser.CommonPart;

import java.util.HashMap;
import java.util.Map;

public abstract class SqlPartDefinition {

    public static Map<CommonPart, String> sqlPartsMap = new HashMap<>();

    static {
        sqlPartsMap.put(CommonPart.FIND_BY_PART, "SELECT *");
        sqlPartsMap.put(CommonPart.FIND_ALL_BY_PART, "SELECT *");
        sqlPartsMap.put(CommonPart.AND_PART, "AND");
        sqlPartsMap.put(CommonPart.OR_PART, "OR");
    }
}
