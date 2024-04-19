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

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExampleResponse {

    public ExampleResponse(Long age, Gender gender, Double experience) {
        this.age = age;
        this.gender = gender;
        this.experience = experience;
    }

    @Column(FieldType.LONG)
    Long age;
    @EnumColumn(EnumType.STRING)
    Gender gender;
    @Column(FieldType.DOUBLE)
    Double experience;
    @Column(FieldType.LONG)
    Long count;
}
