package com.altinntech.clicksave.core.query.parser;

import com.altinntech.clicksave.core.query.builder.QueryType;

import java.util.regex.Pattern;

/**
 * The {@code CommonPart} enum is used to represent common parts of a query.
 * It defines parts such as FIND_BY_PART, FIND_ALL_BY_PART, AND_PART, OR_PART, and CUSTOM_PART.
 *
 * <p>Each part is associated with a {@code QueryType} qualifier.</p>
 *
 * <p>The {@code getPattern()} method returns a regular expression pattern for matching part names.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public enum CommonPart implements Part {

    /**
     * Represents the FIND_BY_PART common part of a query.
     */
    FIND_BY_PART("findBy", QueryType.SELECT),

    /**
     * Represents the FIND_ALL_BY_PART common part of a query.
     */
    FIND_ALL_BY_PART("findAllBy", QueryType.SELECT),

    /**
     * Represents the AND_PART common part of a query.
     */
    AND_PART("And", QueryType.ANY),

    /**
     * Represents the OR_PART common part of a query.
     */
    OR_PART("Or", QueryType.ANY),

    /**
     * Represents the CUSTOM_PART common part of a query.
     */
    CUSTOM_PART("Custom", QueryType.ANY);

    private final String partName;
    private final QueryType qualifier;

    /**
     * Constructs a new CommonPart enum with the specified part name and qualifier.
     *
     * @param partName  the name of the part
     * @param qualifier the qualifier associated with the part
     */
    CommonPart(String partName, QueryType qualifier) {
        this.partName = partName;
        this.qualifier = qualifier;
    }

    @Override
    public String getPartName() {
        return partName;
    }

    @Override
    public boolean isServicePart() {
        return true;
    }

    /**
     * Gets the regular expression pattern for matching part names.
     *
     * @return the regular expression pattern
     */
    public static String getPattern() {
        StringBuilder pattern = new StringBuilder();
        pattern.append("(?:");
        for (Part part : values()) {
            pattern.append(Pattern.quote(part.getPartName())).append("|");
        }
        pattern.deleteCharAt(pattern.length() - 1); // Remove the extra "|" character
        pattern.append(")");
        return pattern.toString();
    }

    /**
     * Gets the qualifier associated with the part.
     *
     * @return the qualifier
     */
    public QueryType getQualifier() {
        return qualifier;
    }
}
