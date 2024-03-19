package com.altinntech.clicksave.enums;

import lombok.Getter;

@Getter
public enum SystemField {

    Sign("cs_sys_sign", "Int8"),
    Version("cs_sys_version", "UInt32"),
    ;

    final private String name;
    final private String type;

    SystemField(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name + " " + type;
    }
}
