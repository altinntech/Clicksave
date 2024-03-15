package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.Embeddable;
import com.altinntech.clicksave.annotations.Embedded;
import com.altinntech.clicksave.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Embeddable
public class EmployeeInfo {

    public EmployeeInfo() {
    }

    @Column(FieldType.STRING)
    String description;
    @Column(FieldType.DOUBLE)
    Double experience;
    @Embedded
    WorkInfo workInfo;
}
