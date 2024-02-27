package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.annotations.Batching;
import com.altinntech.clicksave.annotations.ClickHouseEntity;
import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@ClickHouseEntity // you should use this annotation for persistence entity
@Batching(batchSize = 3) // add batch for saving
public class Person {

    // entity class must have a no arguments constructor
    public Person() {
    }

    @Column(value = FieldType.LONG, id = true)
    Long id;
    @Column(FieldType.STRING)
    String name;
    @Column(FieldType.STRING)
    String lastName;
    @Column(FieldType.INT)
    Integer age; // not supported primitive types
    @Column(FieldType.STRING)
    String address;
    @EnumColumn(EnumType.STRING) // for enum use @EnumColumn annotation
    Gender gender;
    @EnumColumn(EnumType.BY_ID) // you can persist the enum by id value (enum must implements the EnumId interface!)
    Job job;
    String noSaveField; // this field will not be saved

    public static Person buildMockPerson() {
        Person person = new Person();
        person.name = CSUtils.generateRandomString(5);
        person.lastName = CSUtils.generateRandomString(10);
        person.age = CSUtils.generateRandomNumber(1, 99);
        person.address = CSUtils.generateRandomString(15);
        person.gender = CSUtils.getRandomEnum(Gender.class);
        person.job = CSUtils.getRandomEnum(Job.class);
        return person;
    }
}
