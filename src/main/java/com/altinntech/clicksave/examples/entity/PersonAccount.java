package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.annotations.ClickHouseEntity;
import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@ClickHouseEntity(forTest = true)
public class PersonAccount {

    public PersonAccount() {
    }

    @Column(value = FieldType.UUID, id = true)
    UUID id;

    @Column(value = FieldType.STRING)
    String data;
}
