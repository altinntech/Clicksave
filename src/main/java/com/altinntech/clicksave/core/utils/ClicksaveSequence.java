package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.OrderBy;
import com.altinntech.clicksave.annotations.SystemTable;
import com.altinntech.clicksave.enums.FieldType;
import lombok.Data;

@SystemTable
@OrderBy("(timestamp)")
@Data
public class ClicksaveSequence {

    public ClicksaveSequence() {
    }

    @Column(value = FieldType.LONG, id = true)
    Long timestamp;

    @Column(FieldType.STRING)
    String tableName;

    @Column(FieldType.LONG)
    Long startLockId;

    @Column(FieldType.LONG)
    Long endLockId;

    @Column(FieldType.INT)
    Integer isLocked;
}
