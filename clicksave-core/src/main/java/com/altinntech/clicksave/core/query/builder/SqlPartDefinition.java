package com.altinntech.clicksave.core.query.builder;

import com.altinntech.clicksave.core.query.parser.CommonPart;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code SqlPartDefinition} class provides static definitions for SQL parts used in queries.
 * It maps common parts of queries to their corresponding SQL representations.
 *
 * @author Fyodor Plotnikov
 */
public abstract class SqlPartDefinition {

    /**
     * The map containing SQL parts and their representations.
     */
    public static Map<CommonPart, String> sqlPartsMap = new HashMap<>();

    static {
        sqlPartsMap.put(CommonPart.FIND_BY_PART, "SELECT *");
        sqlPartsMap.put(CommonPart.FIND_ALL_BY_PART, "SELECT *");
        sqlPartsMap.put(CommonPart.AND_PART, "AND");
        sqlPartsMap.put(CommonPart.OR_PART, "OR");
        sqlPartsMap.put(CommonPart.CUSTOM_PART, "");
    }
}
