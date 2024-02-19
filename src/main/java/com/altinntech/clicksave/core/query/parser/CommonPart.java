package com.altinntech.clicksave.core.query.parser;

import com.altinntech.clicksave.core.query.builder.QueryType;

import java.util.regex.Pattern;

public enum CommonPart implements Part {

    FIND_BY_PART("findBy", QueryType.SELECT),
    FIND_ALL_BY_PART("findAllBy", QueryType.SELECT),
    AND_PART("And", QueryType.ANY),
    OR_PART("Or", QueryType.ANY),
    CUSTOM_PART("Custom", QueryType.ANY)
    ;

    CommonPart(String partName, QueryType qualifier) {
        this.partName = partName;
        this.qualifier = qualifier;
    }

    private String partName;
    private QueryType qualifier;

    @Override
    public String getPartName() {
        return partName;
    }

    @Override
    public boolean isServicePart() {
        return true;
    }

    public static String getPattern() {
        StringBuilder pattern = new StringBuilder();
        pattern.append("(?:");
        for (Part part : values()) {
            pattern.append(Pattern.quote(part.getPartName())).append("|");
        }
        pattern.deleteCharAt(pattern.length() - 1); // Удаляем лишний символ "|"
        pattern.append(")");
        return pattern.toString();
    }

    public QueryType getQualifier() {
        return qualifier;
    }
}
