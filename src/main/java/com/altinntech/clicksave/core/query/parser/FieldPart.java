package com.altinntech.clicksave.core.query.parser;

/**
 * The {@code FieldPart} class represents parts of a query that correspond to fields.
 * It stores the name of the part.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public class FieldPart implements Part {

    /**
     * The name of the part.
     */
    String partName;

    /**
     * Constructs a new FieldPart instance with the specified part name.
     *
     * @param partName the name of the part
     */
    public FieldPart(String partName) {
        this.partName = partName;
    }

    @Override
    public String getPartName() {
        return partName;
    }

    @Override
    public boolean isServicePart() {
        return false;
    }
}
