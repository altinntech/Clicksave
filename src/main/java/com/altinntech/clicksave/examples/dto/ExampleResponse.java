package com.altinntech.clicksave.examples.dto;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import com.altinntech.clicksave.examples.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExampleResponse {

    @Column(FieldType.LONG)
    Long age;
    @EnumColumn(EnumType.STRING)
    Gender gender;
    @Column(FieldType.DOUBLE)
    Double experience;
}
