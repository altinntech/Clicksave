package com.altinntech.clicksave.enums;

public enum FieldType {

    NONE("Nothing"),
    INT("Int32"),
    LONG("Int64"),
    FLOAT("Float32"),
    DOUBLE("Float64"),
    STRING("String"),
    UINT16("UInt16"),
    //todo: refactor column type
    BIG_DECIMAL("Decimal"),
    UUID("UUID"),
    ;


    FieldType(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }
}
