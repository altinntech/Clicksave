package com.altinntech.clicksave.core.query.parser;

public class FieldPart implements Part {

    String partName;

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
